package yhsb.cjb.db

import grails.gorm.annotation.Entity
import org.grails.datastore.gorm.GormEntity
import org.grails.orm.hibernate.HibernateDatastore
import org.hibernate.annotations.common.util.StringHelper
import org.hibernate.cfg.DefaultNamingStrategy
import org.hibernate.cfg.ImprovedNamingStrategy
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
class FpHistoryData implements FpData, GormEntity<FpHistoryData> {
    static mapping = {
        table 'fphistorydata'
        version false
        id name: 'no'
        naming_strategy new DefaultNamingStrategy()
    }
}

class CustomNamingStrategy extends ImprovedNamingStrategy {
    String classToTableName(String className) {
        println className
        className
    }

    String propertyToColumnName(String propertyName) {
        println propertyName
        propertyName
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