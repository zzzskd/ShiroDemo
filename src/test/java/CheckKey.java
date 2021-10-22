import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.crypto.AesCipherService;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ByteSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class CheckKey {
    public byte[] generatePrincipalCollection() throws IOException {
        SimplePrincipalCollection simplePrincipalCollection = new SimplePrincipalCollection();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(simplePrincipalCollection);
        return byteArrayOutputStream.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        CheckKey checkKey = new CheckKey();
        byte[] temp = checkKey.generatePrincipalCollection();

        AesCipherService aesCipherService = new AesCipherService();
        // byte[] key = Base64.decode(CodecSupport.toBytes("kPH+bIxk5D2deZiIxcaaaA=="));
        byte[] key = Base64.decode(CodecSupport.toBytes("U3ByaW5nQmxhZGUAAAAAAA=="));

        ByteSource ciphertext = aesCipherService.encrypt(temp, key);
        aesCipherService.decrypt(ciphertext.getBytes(), key);
        System.out.println(ciphertext.toString());
    }
}
