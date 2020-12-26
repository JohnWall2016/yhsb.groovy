package yhsb.cjb.db

import grails.gorm.annotation.Entity
import org.grails.datastore.gorm.GormEntity

trait FpData {
    int no //
    String xzj
    String csq
    String address
    String name
    String idCard
    String birthDay
    String pkrk
    String pkrkDate
    String tkry
    String tkryDate
    String qedb
    String qedbDate
    String cedb
    String cedbDate
    String yejc
    String yejcDate
    String ssjc
    String ssjcDate
    String sypkry
    String jbrdsf
    String jbrdsfFirstDate
    String jbrdsfLastDate
    String jbcbqk
    String jbcbqkDate
}

@Entity
class FpHistoryData implements FpData, GormEntity<FpHistoryData> {
    static mapping = {
        table 'fphistorydata'
    }
}