package yhsb.cjb.db

Jzfp2020Db.datastore.withNewSession {
    def d = FpHistoryData.where {
        idCard == '43030220081126003X'
    }
    println d.first()
}

