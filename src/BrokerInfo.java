import java.math.BigInteger;

public class BrokerInfo {
    private BigInteger maxHash;
    private String ip;

    public BrokerInfo(String ip){
        this.ip=ip;
        maxHash=MD5.getMd5(ip+String.valueOf(9090));
    }

    public BigInteger getMaxHash() {
        return maxHash;
    }

    public void setMaxHash(BigInteger maxHash) {
        this.maxHash = maxHash;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
