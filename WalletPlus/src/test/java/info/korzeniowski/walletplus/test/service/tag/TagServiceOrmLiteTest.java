package info.korzeniowski.walletplus.test.service.tag;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.inject.Inject;

import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.module.TestDatabaseModule;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import pl.wkr.fluentrule.api.FluentExpectedException;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TagServiceOrmLiteTest {

    @Inject
    TagService tagService;

    @Inject
    WalletService walletService;

    @Inject
    CashFlowService cashFlowService;

    @Rule
    public FluentExpectedException exception = FluentExpectedException.none();

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).addModules(new TestDatabaseModule(Robolectric.application));
        ((TestWalletPlus) Robolectric.application).inject(this);
    }

    @Test
    public void shouldGenerateColorForTagAfterInsert() {
        // given
        Tag tag = new Tag().setName("tag-1");

        // when
        tagService.insert(tag);

        // then
        assertThat(tagService.findById(tag.getId()).getColor()).isNotNull();
        assertThat(tagService.findById(tag.getId()).getColor()).isNotEqualTo(0);
    }

    @Test
    public void shouldThrowExceptionWhenCreateTagWithoutName() {
        // then
        exception.expect(EntityPropertyCannotBeNullOrEmptyException.class)
                .hasMessageContaining(Tag.NAME_COLUMN_NAME);

        tagService.insert(new Tag());
    }

    @Test
    public void shouldThrowExceptionWhenCreateTagWithDuplicatedId() {
        // given
        Tag tag = new Tag().setName("tag-1");
        tagService.insert(tag);

        // then
        exception.expect(EntityAlreadyExistsException.class);
        tagService.insert(new Tag().setName("tag-2").setId(tag.getId()));
    }

    @Test
    public void shouldReturnNullWhenTryingToFindNonExistingTagById() {
        // given
        Long categoryId = (long) 5326432;

        // then
        assertThat(tagService.findById(categoryId)).isNull();
    }

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
        CashFlow cashFlow2 = new CashFlow().setAmount(50.0).addTag(tag1).setType(CashFlow.Type.EXPANSE).setWallet(wallet);
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
