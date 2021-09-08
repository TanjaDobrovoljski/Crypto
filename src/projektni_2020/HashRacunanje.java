package projektni_2020;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class HashRacunanje {


		
		static MessageDigest dgst;
		
		public static String getHash(byte[] tekst) throws NoSuchAlgorithmException
		{
			dgst = MessageDigest.getInstance("SHA-256");
			StringBuilder s = new StringBuilder();
			byte[] hash = dgst.digest(tekst);
			for(byte b : hash)
			{
				s.append(String.format("%02x", b)); // pretvarala sam heseve u heksadecimalan oblik, jer mi nije radilo poredjenje stringova kod uni code karaktera
			}
			return s.toString();
		}
		
		

	}



