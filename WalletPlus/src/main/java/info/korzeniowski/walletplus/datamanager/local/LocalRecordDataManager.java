package info.korzeniowski.walletplus.datamanager.local;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.datamanager.RecordDataManager;
import info.korzeniowski.walletplus.model.Record;
import info.korzeniowski.walletplus.model.greendao.GreenRecord;
import info.korzeniowski.walletplus.model.greendao.GreenRecordDao;

public class LocalRecordDataManager implements RecordDataManager{
    private GreenRecordDao greenRecordDao;
    private List<Record> records;


    @Inject
    public LocalRecordDataManager(GreenRecordDao greenRecordDao) {
        this.greenRecordDao = greenRecordDao;
        records = getAll();
    }

    @Override
    public Long count() {
        return (long) records.size();
    }

    @Override
    public Record findById(final Long id) {
        Preconditions.checkNotNull(id);

        return Iterables.find(records, new Predicate<Record>() {
            @Override
            public boolean apply(Record record) {
                return Objects.equal(record.getId(), id);
            }
        });
    }

    @Override
    public List<Record> getAll() {
        return getCategoryListFromGreenCategoryList(greenRecordDao.loadAll());
    }

    private List<Record> getCategoryListFromGreenCategoryList(List<GreenRecord> greenRecords) {
        List<Record> recordList = new ArrayList<Record>();
        for(GreenRecord greenRecord: greenRecords) {
            recordList.add(GreenRecord.toRecord(greenRecord));
        }
        return recordList;
    }

    @Override
    public void update(Record record) {
        validateUpdate(record);
        Record toUpdate = findById(record.getId());
        toUpdate.setAmount(record.getAmount());
        toUpdate.setCategoryId(record.getCategoryId());
        toUpdate.setDescription(record.getDescription());
        toUpdate.setDateTime(record.getDateTime());
        greenRecordDao.update(new GreenRecord(record));
    }

    private void validateUpdate(Record record) {
    }

    @Override
    public Long insert(Record entity) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
