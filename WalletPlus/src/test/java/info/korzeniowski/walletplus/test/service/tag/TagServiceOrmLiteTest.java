package info.korzeniowski.walletplus.test.service.tag;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.module.TestDatabaseModule;
import info.korzeniowski.walletplus.service.TagService;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TagServiceOrmLiteTest {

    @Inject
    TagService tagService;

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).addModules(new TestDatabaseModule(Robolectric.application));
        ((TestWalletPlus) Robolectric.application).inject(this);
    }

    /**
     * *************
     * TEST INSERT *
     * *************
     */
    @Test
    public void shouldInsertMainAndTwoSubCategories() {
        Integer mainCategorySize = tagService.getAll().size();

        tagService.insert(new Tag().setName("Main"));

        assertThat(tagService.getAll()).hasSize(mainCategorySize + 1);
    }

    /**
     * ***********
     * TEST READ *
     * ***********
     */
    @Test
    public void shouldReturnNullWhenTryingToFindNonExistingCategoryById() {
        Long categoryId = (long) 5326432;
        assertThat(tagService.findById(categoryId)).isNull();
    }

    @Test
    public void shouldReadMainCategory() {
        String categoryName = "Main";
        Tag inserted = new Tag().setName(categoryName);
        Long id = tagService.insert(inserted);

        Tag read = tagService.findById(id);

        assertThat(inserted).isEqualTo(read);
    }

    /**
     * ***********
     * TEST EDIT *
     * ***********
     */
    @Test
    public void shouldEditNameInMainCategoryWithoutChildren() {
        Tag oldTag = new Tag().setName("Main 1");
        tagService.insert(oldTag);

        Tag newTag = new Tag().setId(oldTag.getId()).setName("Main 1 Fix");
        tagService.update(newTag);

        Tag read = tagService.findById(newTag.getId());
        assertThat(oldTag.getId()).isEqualTo(newTag.getId());
        assertThat(read).isEqualTo(newTag);
    }

    @Test
    public void shouldEditNameInMainCategoryWithChildren() {
        insertMainAndSubs(new Tag().setName("Main 1"), 2);
        Tag main2 = insertMainAndSubs(new Tag().setName("Main 2"), 3);

        Long oldCategoryCount = tagService.count();
        Integer oldMainSize = tagService.getAll().size();

        Tag read = tagService.findById(main2.getId());
        tagService.update(read.setName("Main 2 Fix"));

        assertThat(tagService.count()).isEqualTo(oldCategoryCount);
        assertThat(tagService.getAll()).hasSize(oldMainSize);
    }

    private Tag insertMainAndSubs(Tag tag, Integer numberOfChildren) {
        tagService.insert(tag);
        for (int i = 0; i < numberOfChildren; i++) {
            tagService.insert(new Tag().setName(getSubName(i, tag.getName())));
        }
        return tag;
    }

    private String getSubName(int number, String mainName) {
        return "Sub " + number + " of " + mainName;
    }

    /**
     * *************
     * TEST DELETE *
     * *************
     */
    @Test
    public void shouldDeleteMainCategoryWithoutSubs() {
        Long categoryId = tagService.insert(new Tag().setName("Main"));

        int mainCategories = tagService.getAll().size();
        Long count = tagService.count();
        tagService.deleteById(categoryId);

        assertThat(tagService.getAll()).hasSize(mainCategories - 1);
        assertThat(tagService.count()).isEqualTo(count - 1);
    }
}
