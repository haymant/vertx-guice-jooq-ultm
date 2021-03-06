package io.github.qrman.potato.control;

import com.github.witoldsz.ultm.TxManager;
import static io.github.qrman.potato.db.Tables.POTATO;
import static io.github.qrman.potato.db.Tables.POTATO_BAG;
import io.github.qrman.potato.db.TxJooq;
import io.github.qrman.potato.db.tables.records.PotatoBagRecord;
import io.github.qrman.potato.db.tables.records.PotatoRecord;
import io.github.qrman.potato.entity.Potato;
import io.github.qrman.potato.entity.PotatoBag;
import javax.inject.Inject;
import org.jooq.DSLContext;

public class BasementStore {

    private final TxManager txManager;
    private final DSLContext txJooq;
    private final PotatoQualityChecker qualityChecker;

    @Inject
    public BasementStore(TxManager txManager, @TxJooq DSLContext txJooq, PotatoQualityChecker qualityChecker) {
        this.txManager = txManager;
        this.txJooq = txJooq;
        this.qualityChecker = qualityChecker;
    }

    public void store(PotatoBag potatoBag) {
        txManager.tx(() -> {
            PotatoBagRecord potatoBagRecord = txJooq.newRecord(POTATO_BAG);
            potatoBagRecord.setOrigin(potatoBag.getOrigin());
            potatoBagRecord.store();

            potatoBag.getItems().stream()
              .forEach((Potato potato) -> {
                  qualityChecker.check(potato);
                  storePotato(potato, potatoBagRecord);
              });
        });
    }

    private void storePotato(Potato potato, PotatoBagRecord potatoBagRecord) {
        PotatoRecord potatoRecord = txJooq.newRecord(POTATO);
        potatoRecord.setBag(potatoBagRecord.getId());
        potatoRecord.setQuality(potato.getQuality());
        potatoRecord.store();
    }

}
