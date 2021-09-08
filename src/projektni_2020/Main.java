package projektni_2020;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.sql.SQLClientInfoException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.imageio.ImageIO;

import org.bouncycastle.crypto.tls.TlsAuthentication;
import org.bouncycastle.jce.provider.BouncyCastleProvider;




//import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Main {
	public static boolean pogresanSert=false;
	public static boolean slika=true;
	 public static void main(String[] args) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, CertificateException, SignatureException, IOException, NoSuchProviderException
	 {
	
		 Security.addProvider(new BouncyCastleProvider());
		 
		 ProvjeraSertifikata ca=new ProvjeraSertifikata();
		 int indexSl=0;
		 Korisnik.procitajNaloge();
		 Scanner s1 = new Scanner(System.in);
		 
		 while(true) {		 
			 while (Korisnik.trenutniPrijavljeni == null) 
			 {
				 System.out.println("Unesite korisnicko ime :");
				 
				 
				 String ime=new Scanner(System.in).nextLine();
				 
				 System.out.println("Unesite lozinku :");
			
				 String lozinka= new Scanner(System.in).nextLine();
				 
				 String hloz= HashRacunanje.getHash(lozinka.getBytes());
				 
				 Korisnik.trenutniPrijavljeni=Korisnik.ispravnaPrijava(ime, hloz);
			 }
			 
			 
			 X509Certificate korisnikov_cert=ProvjeraSertifikata.napraviSertifikat(Korisnik.trenutniPrijavljeni.putanjaDoSertifikata.toString()); 
			 
			 if (ProvjeraSertifikata.provjeriSertifikat(korisnikov_cert) && !ProvjeraSertifikata.daLiJePovucen(korisnikov_cert)) 
			 {
				 
				 System.out.println("Uspjesno ste se ulogovali!");	
				 //prva poruka mora biti slika

				 String tmp="";
				 
				 while (true) 
				 {
					 tmp="";
					 System.out.println("Lista korisnika za komunikaciju (izaberite korisnika) ili ukucajte exit za log out ili ukucajte poruka za citanje inboxa: ");
					 
					 for(Korisnik k:Korisnik.kreiraniNalozi)
					 {
						 if (!k.ime.equals(Korisnik.trenutniPrijavljeni.ime))
						 {
							 System.out.println(k.ime);
							 
						 }
					 }
					 
					 
					 tmp=new Scanner(System.in).nextLine();
					 
					 if (tmp.equals("exit")) break;
					 
					 else if (tmp.equals("poruka"))
							 {
						 			procitaj();
							 }
					 else {
						// System.out.println("------------"+tmp);
						 Poruka.primalac=new Korisnik(tmp);
						 Poruka porukaZaSlanjePoruka=null;
						 if(Poruka.prvaKomunikacija) {
							 boolean flag=true;
							 while(flag)
							 {
								 System.out.println("Unesite putanju do slike");
								 String put=new Scanner(System.in).nextLine();
								 Poruka.slikaZaSteg = ImageIO.read(new File("slikeKripto"+File.separator+put+".jpg"));
								 porukaZaSlanjePoruka=kreirajPoruku();
								 if(!pogresanSert) {
									 porukaZaSlanjePoruka.kreirajBajtoveZaSliku();
									 if(porukaZaSlanjePoruka.prevelikaPorukaZaDatuSliku())
									 {
										 System.out.println("Poruka je prevelika za datu sliku! Unesite novu putanju slike ili novu poruku.");
									 }
									 else {
										 flag=false;
										 Steganografija.smjestiBajtoveUSliku(porukaZaSlanjePoruka.sviBajtoviZaSliku);
								           BufferedImage copy=porukaZaSlanjePoruka.slikaZaSteg.getSubimage(0, 0, porukaZaSlanjePoruka.slikaZaSteg.getWidth(), porukaZaSlanjePoruka.slikaZaSteg.getHeight());
								            File file=new File("inbox"+File.separator+Poruka.primalac.ime+File.separator+Korisnik.trenutniPrijavljeni.ime+File.separator+"novaSlika"+indexSl+".png");
								            indexSl++;
								            try {
												ImageIO.write(copy, "PNG", file);
											} catch (IOException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}
								            Poruka.prvaKomunikacija=false;
									 }
								 }
								 else {
									flag=false;
								}
							 }
						 }
						 else {
							 porukaZaSlanjePoruka=kreirajPoruku();
							 if(!pogresanSert) {
								 porukaZaSlanjePoruka.sacuvaj();
								 System.out.println("Uspijesno ste poslali poruku !");
							 }
						 }
					   
				 }
					 }
				 if (!pogresanSert)
					 System.out.println("Uspijesno ste se izlogovali !");
				 Korisnik.trenutniPrijavljeni=null;
				 
			 
				 }
			 else {
				System.out.println("Sertifikat nije validan ili je povucen!");
				//break;
			}
		
		 }
		 }
	 public static void procitaj()
	 {
		 File file=null;
		 for(int i=1;i<=4;i++)
		 {
			 if(!Korisnik.trenutniPrijavljeni.ime.endsWith(String.valueOf(i)))
			 {
				// System.out.println(Korisnik.trenutniPrijavljeni.ime+" dfsdcec "+"korisnik"+String.valueOf(i));
				 file=new File("inbox"+File.separator+Korisnik.trenutniPrijavljeni.ime+File.separator+"korisnik"+String.valueOf(i));
				 
				for (File f : file.listFiles())
				{
					
				   if (f.getName().endsWith(".txt"))
				   {
					   String poruka=Poruka.citanje(f);
						System.out.println(poruka);
				   }
				   else if(f.getName().endsWith(".png"))
				   {
					   try {
							if(Poruka.verifikacijaPoruke(f))
							{
								System.out.println("Uspijesna verifikacija");
								System.out.println("Nova poruka!\nPosiljalac: "+Poruka.originalnaPoruka.split("@@@@@")[1]+ "\nDatum: "+ Poruka.originalnaPoruka.split("@@@@@")[2]+ "\nPoruka: "+ Poruka.originalnaPoruka.split("@@@@@")[0]+"\n");
								f.delete();
							}
						} catch (Exception e) {
							e.printStackTrace();
						} 
				   }
				}
				 
			 }
		 }
		
	 }
	 public static Poruka kreirajPoruku() throws CertificateException, IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, SignatureException {
		 X509Certificate primaoc_cert=ProvjeraSertifikata.napraviSertifikat(Poruka.primalac.putanjaDoSertifikata.toString()); 
		Poruka poruka=null;
		 if (ProvjeraSertifikata.provjeriSertifikat(primaoc_cert) && !ProvjeraSertifikata.daLiJePovucen(primaoc_cert)) {
		 
			 System.out.println("Unesite poruku koju zelite da posaljete odabranom korisniku:");

		     poruka=new Poruka(new Scanner(System.in).nextLine());
		     if(Poruka.prvaKomunikacija)
		     {
		    	 poruka.kriptujPorukuStego();///////////////////////////////////////////////////////stego je samo ako je slika a ne obicna por
		     }
		     else 
		     {
				poruka.kriptujPoruku();
		     }
			
		     poruka.kriptujKljuc();
		     poruka.potpis();
		     
		     return poruka;
		     
		 }
	     else {
				System.out.println("Sertifikat primaoca nije validan ili je povucen!");
				 pogresanSert=true;
			
			}
		 return poruka;
		
	 }
	
}
