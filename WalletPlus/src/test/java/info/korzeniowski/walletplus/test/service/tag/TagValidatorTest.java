package info.korzeniowski.walletplus.test.service.tag;

import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.UUID;

import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.model.TagAndCashFlowBind;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.service.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.ormlite.TagServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.validation.TagValidator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TagValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private TagService tagService;

    private TagService validatorService;

    @Before
    public void setUp() {
        @SuppressWarnings("unchecked")
        Dao<Tag, Long> tagDao = mock(Dao.class);
        Dao<CashFlow, Long> cashFlowDao = mock(Dao.class);
        Dao<TagAndCashFlowBind, Long> tagAndCashFlowBinds = mock(Dao.class);
        validatorService = mock(TagService.class);
        //TODO: Czy Dagger może się tym zająć?
        tagService = new TagServiceOrmLite(tagDao, cashFlowDao, tagAndCashFlowBinds, new TagValidator(validatorService));
    }

    /**
     * *******************
     * INSERT VALIDATION *
     * *******************
     */
    @Test
    public void shouldInsertMainCategory() {
        tagService.insert(getSimpleMainIncomeCategory());
    }

    @Test
    public void shouldThrowExceptionWhenInsertCategoryWithoutName() {
        exception.expect(EntityPropertyCannotBeNullOrEmptyException.class);
        exception.expectMessage("Name");
        tagService.insert(getSimpleMainIncomeCategory().setName(null));
    }

    @Test
    public void shouldThrowExceptionWhenInsertWithDuplicatedId() {
        Tag toInsert = getSimpleMainIncomeCategory();
        when(validatorService.findById(toInsert.getId())).thenReturn(toInsert);

        exception.expect(EntityAlreadyExistsException.class);
        tagService.insert(getSimpleMainIncomeCategory().setId(toInsert.getId()));
    }

    /**
     * *******************
     * DELETE VALIDATION *
     * *******************
     */
    @Test
    public void shouldDeleteMainCategory() {
        Tag main = getSimpleMainIncomeCategory();
        when(validatorService.findById(main.getId())).thenReturn(main);

        tagService.deleteById(main.getId());
    }

    /**
     * *******************
     * UPDATE VALIDATION *
     * *******************
     */
    @Test
    public void shouldUpdateNameOfCategory() {
        Tag tag = getSimpleMainIncomeCategory();
        String newName = "NewName";
        when(validatorService.findById(tag.getId())).thenReturn(tag);
        when(validatorService.findByName(newName)).thenReturn(null);

        tagService.update(tag.setName(newName));
    }

    private Tag getSimpleMainIncomeCategory() {
        UUID id = UUID.randomUUID();
        return new Tag()
                .setName("Simple category-" + id.getMostSignificantBits())
                .setId(id.getMostSignificantBits());
    }
}
