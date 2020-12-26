package yhsb.cjb.db

import grails.gorm.annotation.Entity
import groovy.transform.ToString
import org.grails.datastore.gorm.GormEntity
import org.grails.orm.hibernate.HibernateDatastore

@Entity
@ToString
class JbHistoryData implements GormEntity<JbHistoryData> {
    String idCard
    String name

    static mapping = {
        table '居保历史参保数据20180314'
        version false
        id name: 'idCard'
        idCard column: '公民身份证号码'
        name column: '姓名'
    }
}

class MainCase {
    static void main(String[] args) {
        def datastore = new HibernateDatastore([
                'dataSource.driverClassName': 'com.mysql.cj.jdbc.Driver',
                'dataSource.url'            : 'jdbc:mysql://localhost:3306/test?characterEncoding=utf8',
                'dataSource.username'       : 'root',
                'dataSource.password'       : 'root',
                'dataSource.logSql'         : 'true',
        ], JbHistoryData)

        datastore.withNewSession {
            def d = JbHistoryData
                    .get('130321196403220601')
            println d

            println d.class.getClassLoader()
            println JbHistoryData.getClassLoader()
        }
    }
}
