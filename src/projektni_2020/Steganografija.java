package projektni_2020;


import java.awt.image.BufferedImage;

public class Steganografija {
	
	static int vrsta;
	static int kolona;
	
	public static Poruka procitana;

	public static void smjestiBajtoveUSliku(byte[] sviBajtoviZaSliku)//2
	{
		Steganografija.vrsta = 0;
		Steganografija.kolona = 0;
		for(int i = 0; i < sviBajtoviZaSliku.length; i++)
		{
			smjestiJedanBajt(sviBajtoviZaSliku[i]);
		}
	}
	
	static void povecajBrojace()
	{
		int sirina = Poruka.slikaZaSteg.getWidth();
		int visina = Poruka.slikaZaSteg.getHeight();
		
		
		
		if((Steganografija.kolona + 1) < sirina)
		{		
			Steganografija.kolona ++;
		}
		else if((Steganografija.kolona + 1) >= sirina  && Steganografija.vrsta < (visina-1))
		{
			Steganografija.kolona = 0;
			Steganografija.vrsta ++;
		}	
		
		
		
	}
	
	public static void smjestiJedanBajt(byte bajt)
	{
		int bit = 7;
		while(bit > -1)
		{
			int bajt2 = bajt >>> bit; // sift za toliko bitova
			int bajt3 = bajt2 & 1; // dobijem vrijednost bita  NA poruci je bajt
			
			
			int rgbNaSlici = Poruka.slikaZaSteg.getRGB(Steganografija.kolona, Steganografija.vrsta);
			if((rgbNaSlici % 2 ) == 0) // zadnji bit na slici 0
			{
				if(bajt3 == 1)		
				{
					rgbNaSlici ++;
				}
			}
			else
			{
				//  zadnji bit na slici je 1
				if(bajt3 == 0)
				{
					rgbNaSlici--;
				}
			}		
			
			Poruka.slikaZaSteg.setRGB(Steganografija.kolona, Steganografija.vrsta, rgbNaSlici);
			Steganografija.povecajBrojace();
			bit--;
		}
	}
	
	
	public static Poruka procitajPoruku(BufferedImage slika)//3
	{
		int sirina = slika.getWidth();
		int visina = slika.getHeight();
		
		int duzinaKljucaSlika = 0;
		int duzinaPorukeSlika = 0;
		int duzinaPotpisaSlika = 0;
		
		
		byte[] kriptovanKljucSlika =null;
		byte[] kriptovanaPorukaSlika=null;
		byte[] potpisSlika=null;
		
		
		int br = 0;
		int bit = 7;
		
		int bajt = 0;
		int bajt2 = 0;
		int bajt3 = 0;
		
		for(int i = 0 ; i < visina ; i++)
		{
			for(int j = 0; j < sirina ; j++)
			{
				br++;
				if(br <= 32) // citam duzinu Kriptovnog Kljuca 
				{
					int rgbKomponenta = slika.getRGB(j, i);
					if((rgbKomponenta % 2 )!= 0)
					{
						duzinaKljucaSlika += Math.pow(2.0, 32-br);
					}
					if(br==32)	{ 
						kriptovanKljucSlika = new byte[duzinaKljucaSlika];	
						
						} 
				}
				else if( br <= 64) // citam duzinu Kriptovane Poruke
				{
					int rgbKomponenta = slika.getRGB(j, i);
					if(rgbKomponenta % 2 != 0)
					{
						duzinaPorukeSlika += Math.pow(2.0,64- br);
					}
					if(br==64)	{ kriptovanaPorukaSlika = new byte[duzinaPorukeSlika];} 
				}
				else if( br <= 96 ) // citam duzinu potpisa
				{
					int rgbKomponenta = slika.getRGB(j, i);
					if(rgbKomponenta % 2 != 0)
					{
						duzinaPotpisaSlika += Math.pow(2.0, 96-br);
					}
					if(br == 96) { potpisSlika = new byte[duzinaPotpisaSlika];}
				}
				else if( br <=  ((duzinaKljucaSlika*8) + 96)) // citam kriptovan kljuc
				{
					int rgbKomponenta = slika.getRGB(j, i);
					if(rgbKomponenta % 2 != 0)
					{
						// kriptovan kljuc 
						kriptovanKljucSlika[bajt] += Math.pow(2.0, bit);
					}
					bit--; if(bit < 0) {bit = 7;  bajt ++;} 
					  
					// ako je doslo do zadnjeg bajta stavi abjt na nula
				}
				else if(br <= (duzinaPorukeSlika*8  + 96 + duzinaKljucaSlika*8 ))
				{
					int rgbKomponenta = slika.getRGB(j, i);
					if(rgbKomponenta % 2 != 0)
					{
						// kriptovana poruka 
						kriptovanaPorukaSlika[bajt2] += Math.pow(2.0, bit);
					}
					bit--; if(bit < 0) {bit = 7;  bajt2++;} 
				}
				else if(br <= (duzinaPotpisaSlika*8 + 96 + duzinaKljucaSlika*8 + duzinaPorukeSlika*8) )
				{
					int rgbKomponenta = slika.getRGB(j, i);
					if(rgbKomponenta % 2 != 0)
					{
						// potpis 
						potpisSlika[bajt3] += Math.pow(2.0, bit);
					}
					bit--; if(bit < 0) {bit = 7; bajt3++;} 
				}
			
			}
		}
	
		Poruka procitana = new Poruka(kriptovanKljucSlika,kriptovanaPorukaSlika,potpisSlika);
		procitana.slikaZaSteg = slika;
		return procitana;
		
		
	}

	public static byte[] kreirajBajtoveZaSliku(byte[] kljucKriptovan, byte[]porukaKriptovana,byte[]potpis) //1
	{
		byte[] duzinaKljuca = new byte[] {
	            (byte)(kljucKriptovan.length >>> 24),
	            (byte)(kljucKriptovan.length >>> 16),
	            (byte)(kljucKriptovan.length >>> 8),
	            (byte) kljucKriptovan.length};
		
		byte[] duzinaPoruke = new byte[] {
	            (byte)(porukaKriptovana.length >>> 24),
	            (byte)(porukaKriptovana.length >>> 16),
	            (byte)(porukaKriptovana.length >>> 8),
	            (byte) porukaKriptovana.length};
	
		byte[] duzinaPotpisa = new byte[] {
				 (byte)(potpis.length >>> 24),
		         (byte)(potpis.length >>> 16),
		         (byte)(potpis.length >>> 8),
		         (byte) potpis.length};
		
		byte[] sviBajtoviZaSliku = new byte[duzinaKljuca.length + duzinaPoruke.length+ duzinaPotpisa.length + kljucKriptovan.length + porukaKriptovana.length + potpis.length];
		
		for(int i = 0; i< duzinaKljuca.length;i++)
		{
			sviBajtoviZaSliku[i] = duzinaKljuca[i];
		}
		
		for(int i = 0; i< duzinaPoruke.length;i++)
		{
			sviBajtoviZaSliku[duzinaKljuca.length + i] = duzinaPoruke[i];
		}
		
		for(int i = 0; i< duzinaPotpisa.length;i++)
		{
			sviBajtoviZaSliku[duzinaPoruke.length + duzinaKljuca.length + i] = duzinaPotpisa[i];
		}
		
		for(int i = 0; i< kljucKriptovan.length; i++)
		{
			sviBajtoviZaSliku[duzinaPoruke.length + duzinaKljuca.length + duzinaPotpisa.length + i] = kljucKriptovan[i];
		}
		
		for(int i = 0; i< porukaKriptovana.length; i++)
		{
			sviBajtoviZaSliku[duzinaPoruke.length + duzinaKljuca.length + duzinaPotpisa.length+kljucKriptovan.length+i ] = porukaKriptovana[i];
		}
		
		for(int i = 0; i< potpis.length; i++)
		{
			sviBajtoviZaSliku[duzinaPoruke.length + duzinaKljuca.length + duzinaPotpisa.length+kljucKriptovan.length+porukaKriptovana.length+i] = potpis[i];
		}
	
		return sviBajtoviZaSliku;
	

	}
	
}

