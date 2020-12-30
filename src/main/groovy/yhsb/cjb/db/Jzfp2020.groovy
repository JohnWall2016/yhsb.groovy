package yhsb.cjb.db

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import org.grails.datastore.gorm.GormEntity
import org.grails.orm.hibernate.HibernateDatastore
import yhsb.base.util.Config

trait FpData {
    int no
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
@ToString
class FpHistoryData implements FpData, GormEntity<FpHistoryData> {
    static mapping = {
        table 'fphistorydata'
        version false
        id name: 'no'
    }
}

class Jzfp2020Db {
    static private HibernateDatastore datastore

    static HibernateDatastore getDatastore() {
        if (datastore) return datastore
        datastore = new HibernateDatastore(
                Config.load('jzfp2020').toMap(),
                FpHistoryData
        )
        datastore
    }
}