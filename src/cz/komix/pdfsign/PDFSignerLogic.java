package cz.komix.pdfsign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;

public class PDFSignerLogic {

    private String digest;
    private String signature;
    
    /**
     * Pripravuje a spousti proces podepisovani:
     * vytvori potrebne soubory, nastavi podpisova pole,
     * nastavi helper na podepisovani, nakonec spusti podepisovani.
     * 
     * Vola se 2x:
     * 1. ziskani digestu dokumentu
     * 2. podepsani dokumenty
     * Pri druhem volani se vlozeny digest porovnava se spocitanym
     * 
     * @return Digest, "OK" nebo popis chyby
     * @throws Exception
     */
    public String getDigestAndSign() throws Exception {
        
        // helper je trida, kde se provadi vlastni
        // vypocet digestu a vkladani podpisu
        SignerHelper signerHelper = new SignerHelper();
        signerHelper.setDigest(digest);
        signerHelper.setSignature(signature);
        
        
        File document = new File("/tmp/file.pdf");

        // udelam si kopii, do ni budu ukladat podpis
        String name = document.getName();
        String substring = name.substring(0, name.lastIndexOf("."));
        
        File outputDocument = new File(document.getParent(), substring + "_signed.pdf");
        FileInputStream fis = new FileInputStream(document);
        FileOutputStream fos = new FileOutputStream(outputDocument);
        
        byte[] buffer = new byte[8 * 1024];
        int c;
        while ((c = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, c);
        }
        fis.close();
        fis = new FileInputStream(outputDocument);
        
        File scratchFile = File.createTempFile("pdfbox_scratch", ".bin");
        RandomAccessFile randomAccessFile = new RandomAccessFile(scratchFile, "rw");
        
        try {
            // load document
            PDDocument doc = PDDocument.loadNonSeq(document, randomAccessFile);
            
            // create signature dictionary
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("signer name");
            signature.setLocation("signer location");
            signature.setReason("reason for signature");
            
            // datum podpisu se nesmi menit mezi jednotlivymi behy
            Calendar datumPodpisu = Calendar.getInstance();
            datumPodpisu.set(Calendar.MILLISECOND, 0);
            datumPodpisu.set(Calendar.SECOND, 0);
            datumPodpisu.set(Calendar.MINUTE, 0);
            datumPodpisu.set(Calendar.HOUR, 0);
            signature.setSignDate(datumPodpisu);
            
            // verze dokumentu se nesmi menit mezi jednotlivymi behy
            doc.setDocumentId(123L);
            
            doc.addSignature(signature, signerHelper);
            doc.saveIncremental(fis, fos); // ted probehne podepsani
            
        } catch (COSVisitorException e) {
            Throwable cause = e.getCause();
            if (e.getCause() instanceof SignatureException) {
                if ("Posilam digest".equals(cause.getMessage())) {
                    
                    // Pri prvnim volani, kdy ziskavam digest
                    // a jeste nemam podpis, proces prerusim
                    // tim, ze vratim exception
                    
                    return signerHelper.getDigest();
                }
            }
            throw e;
        } finally {
            if (scratchFile != null && scratchFile.exists() && !scratchFile.delete()) {
                scratchFile.deleteOnExit();
            }
        }
        
        return "OK";
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
