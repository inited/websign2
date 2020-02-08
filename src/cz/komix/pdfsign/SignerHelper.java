package cz.komix.pdfsign;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.util.encoders.Base64;

/**
 * Pomocna trida, ktera dela dve veci:
 * 1. odchytne data k podpisu, spocita digest a vrati ho
 * 2. vrati podpis do PDFBoxu
 * 
 * @author jara
 *
 */
public class SignerHelper implements SignatureInterface {

    private String digest;
    private String signature;
    
    public SignerHelper() {
        digest = null;
        signature = null;
    }
    
    public byte[] sign(InputStream content) throws SignatureException, IOException {

        // nacitam to do byte array, kdybych si to
        // pro ucely ladeni chtel take ulozit do souboru
        
        byte[] targetArray = readByteArray(content);
        
        String newDigest = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digestBytes = messageDigest.digest(targetArray);
            newDigest = new String(Base64.encode(digestBytes));
        } catch (NoSuchAlgorithmException e) {
            // pravdepodobne nemam Bouncy Castle knihovny
            throw new SignatureException(e);
        }
        
        if (digest == null) {
            // vsechno v poradku, koncim 1. fazi, vracim digest
            digest = newDigest;
            throw new SignatureException("Posilam digest");
        }
        
        if (!digest.equals(newDigest)) {
            throw new SignatureException("Nesouhlasi digest");
        }

        if ((signature == null) || (signature.length() == 0)) {
            throw new SignatureException("Ocekavam podpis");
        }
        
        return Base64.decode(signature);
    }

    private byte[] readByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        
        // urcite pouzivat tento zpusob a nespolehat na is.available()
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        is.close();
     
        buffer.flush();
        return buffer.toByteArray();
    }
    
    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
