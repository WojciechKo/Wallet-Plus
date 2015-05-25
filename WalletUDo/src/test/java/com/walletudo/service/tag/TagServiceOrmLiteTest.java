package com.walletudo.service.tag;

import android.graphics.Color;
import android.test.suitebuilder.annotation.SmallTest;

import com.walletudo.model.CashFlow;
import com.walletudo.model.Tag;
import com.walletudo.model.Wallet;
import com.walletudo.service.exception.EntityAlreadyExistsException;
import com.walletudo.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import com.walletudo.test.ServiceInjectedUnitTest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import pl.wkr.fluentrule.api.FluentExpectedException;

import static org.assertj.core.api.Assertions.assertThat;

@SmallTest
public class TagServiceOrmLiteTest extends ServiceInjectedUnitTest {

    @Rule
    public FluentExpectedException exception = FluentExpectedException.none();

    @Before
    public void setUp() {
        super.setUp();
    }

    /**
     * CREATE
     */
    @Test
    public void shouldThrowExceptionWhenCreateTagWithoutName() {
        // then
        exception.expect(EntityPropertyCannotBeNullOrEmptyException.class)
                .hasMessageContaining(Tag.NAME_COLUMN_NAME);

        tagService.insert(new Tag());
    }

    @Test
    public void shouldCreateTag() {
        // given
        Tag tag = new Tag().setName("tag-1").setColor(Color.BLUE);

        // when
        tagService.insert(tag);

        // then
        assertThat(tagService.findById(tag.getId())).isEqualTo(tag);
    }

    @Test
    @Ignore
    public void shouldGenerateColorForTagIfNotSpecifiedAfterInsert() {
        // given
        Tag tag = new Tag().setName("tag-1");

        // when
        tagService.insert(tag);

        // then
        assertThat(tagService.findById(tag.getId()).getColor()).isNotNull();
        assertThat(tagService.findById(tag.getId()).getColor()).isNotEqualTo(0);
    }

    @Test
    public void shouldThrowExceptionWhenCreateTagWithDuplicatedName() {
        // given
        String tagName = "tag-1";
        Tag tag = new Tag().setName(tagName);
        tagService.insert(tag);

        // then
        exception.expect(EntityAlreadyExistsException.class);
        tagService.insert(new Tag().setName(tagName).setId(tag.getId()));
    }

    /**
     * READ
     */
    @Test
    public void shouldReturnNullWhenTryingToFindNonExistingTagById() {
        // given
        Long categoryId = (long) 5326432;

        // then
        assertThat(tagService.findById(categoryId)).isNull();
    }

    @Test
    public void shouldTagsBeOrderedByName() {
        // given
        Tag tagAla = new Tag("Ala");
        tagService.insert(tagAla);
        Tag tagBartek = new Tag("Bartek");
        tagService.insert(tagBartek);
        Tag tagCelina = new Tag("Celina");
        tagService.insert(tagCelina);

        // then
        assertThat(tagService.getAll()).containsExactly(tagAla, tagBartek, tagCelina);
    }

    /**
     * UPDATE
     */
    @Test
    public void shouldUpdateTagName() {
        // given
        Tag tag = new Tag().setName("Tag-1");
        tagService.insert(tag);
        String newTagName = "Tag-1-new";

        // when
        tagService.update(tag.setName(newTagName));

        // then
        assertThat(tagService.findById(tag.getId()).getName()).isEqualTo(newTagName);
    }

    /**
     * DELETE
     */
    @Test
    public void shouldDeleteTag() {
        // given
        Tag tag = new Tag().setName("tag-1");
        tagService.insert(tag);
        Long tagCount = tagService.count();

        // when
        tagService.deleteById(tag.getId());

        // then
        assertThat(tagService.count()).isEqualTo(tagCount - 1);
        assertThat(tagService.count()).isEqualTo(tagService.getAll().size());
    }

    @Test
    public void shouldRemoveTagFromAssociatedCashFlowsAfterDelete() {
        // given
        Wallet wallet = new Wallet().setName("wallet").setInitialAmount(100.0);
        walletService.insert(wallet);
        Tag tag1 = new Tag().setName("tag-1");
        tagService.insert(tag1);
        Tag tag2 = new Tag().setName("tag-2");
        tagService.insert(tag2);

        CashFlow cashFlow1 = new CashFlow().setAmount(50.0).addTag(tag1, tag2).setType(CashFlow.Type.INCOME).setWallet(wallet);
        cashFlowService.insert(cashFlow1);
        CashFlow cashFlow2 = new CashFlow().setAmount(50.0).addTag(tag1).setType(CashFlow.Type.EXPENSE).setWallet(wallet);
        cashFlowService.insert(cashFlow2);

        // when
        tagService.deleteById(tag1.getId());

        // then
        assertThat(cashFlowService.findById(cashFlow1.getId()).getTags()).containsOnly(tag2);
        assertThat(cashFlowService.findById(cashFlow2.getId()).getTags()).isEmpty();
        assertThat(tagService.findByName(tag1.getName())).isNull();
        assertThat(tagService.findById(tag1.getId())).isNull();
    }
}
