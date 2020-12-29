package yhsb.cjb.db


TestDb.getDatastore().withNewSession {
    def d = JbHistoryData.get('130321196403220601')
    println d

    println d.class.getClassLoader()
    println JbHistoryData.getClassLoader()

    d = JbHistoryData.find {
        idCard == '130321196403220601'
    }
    println d

    d = JbHistoryData.find {
        idCard == 'NOBODY'
    }
    println d
}

