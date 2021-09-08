package projektni_2020;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.Date;

public class ProvjeraSertifikata {
	public static X509Certificate root;
	
	public ProvjeraSertifikata() {
		try {
		root=napraviSertifikat("digitalniSertifikatiKorisnika"+File.separator+"CA.pem");//bilo je .der
		}catch(IOException e) {
			e.printStackTrace();
		} catch(CertificateException e) {}
	} 
	
	public static X509Certificate napraviSertifikat(String putanja) throws IOException, CertificateException{
	    FileInputStream fin=new FileInputStream(putanja);
	    CertificateFactory cf=CertificateFactory.getInstance("X.509");
	    X509Certificate certificate=(X509Certificate)cf.generateCertificate(fin);
	    fin.close();
	    return certificate;
	}
	
	public static boolean provjeriSertifikat(X509Certificate certificate){
		try {
			
			certificate.verify(root.getPublicKey());
	        certificate.checkValidity(new Date());
		} catch (Exception e) {
	            return false;
	    }
	    return true;
	}
	public static boolean daLiJePovucen(X509Certificate certificate) throws IOException{
        X509CRL crl=null;
        X509CRLEntry povucenSertifikat = null;  
        try {
            DataInputStream inStream = new DataInputStream(new FileInputStream(new File("digitalniSertifikatiKorisnika"+File.separator+"crl.pem")));
            CertificateFactory cf = CertificateFactory.getInstance("X509");
            crl = (X509CRL)cf.generateCRL(inStream);
            inStream.close();
        } catch (FileNotFoundException | CertificateException | CRLException ex) {
        	return true;
        }
        povucenSertifikat = crl.getRevokedCertificate(certificate.getSerialNumber());
        if(povucenSertifikat !=null)
            return true;
        return false;
    
    }   
}
