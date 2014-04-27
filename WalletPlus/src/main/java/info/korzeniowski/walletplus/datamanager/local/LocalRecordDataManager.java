package info.korzeniowski.walletplus.datamanager.local;

import java.util.List;

import javax.inject.Inject;

import info.korzeniowski.walletplus.datamanager.RecordDataManager;
import info.korzeniowski.walletplus.model.Record;
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
        return null;
    }

    @Override
    public Record getById(Long id) {
        return null;
    }

    @Override
    public List<Record> getAll() {
        return null;
    }

    @Override
    public void update(Record entity) {

    }

    @Override
    public Long insert(Record entity) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }
}
