package yhsb.cjb.net

import com.google.gson.annotations.SerializedName

class SysLogin extends Request {

    SysLogin(String userName, String password) {
        super('syslogin')
        this.userName = userName
        this.password = password
    }

    @SerializedName('username')
    String userName

    @SerializedName('passwd')
    String password
}