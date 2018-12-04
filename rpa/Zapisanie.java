import resources.WyszukiwanieKontrahentaHelper;
import tools.DB;
import tools.FileService;
import tools.Parse;
import tools.RobotFields;
import tools.RobotType;
import tools.RobotSearchingGroup;
import tools.RobotSleep;

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;
import com.rational.test.ft.object.interfaces.SAP.*;
import com.rational.test.ft.object.interfaces.WPF.*;
import com.rational.test.ft.object.interfaces.dojo.*;
import com.rational.test.ft.object.interfaces.siebel.*;
import com.rational.test.ft.object.interfaces.flex.*;
import com.rational.test.ft.object.interfaces.generichtmlsubdomain.*;
import com.rational.test.ft.value.*;
import com.rational.test.ft.vp.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Description : Functional Test Script
 * 
 * @author uniteam.lukbed
 */
public class WyszukiwanieKontrahenta extends WyszukiwanieKontrahentaHelper {

	private DB db;

	private String SCRIPT_NAME;

	private RobotFields robotFields;

	private String nip;

	/*
	 * TYLKO TUTAJ MOZNA STEROWAC STATUSEM SKRYPTU
	 */
	public void testMain(Object[] args) {
		try {
			// db = DB.getInstance("33820");
			
			db = DB.getInstance(args[0]);

			SCRIPT_NAME = this.getClass().getName();
			robotFields = new RobotFields(db);

			// JEZELI ROBOT WPISUJE TO NIE robotFields.nip TYLKO POLE nip
			nip = robotFields.nip.replace("-", "").replace("", " ").replace(" ", "%").replaceFirst("%", "");

			boolean wyszukanoPoPpe = false;
			boolean wyszukanoPoNip = false;
			boolean wyszukanoNaLiscie = false;

			przejdzDoEwidencjiKontrahentow();

			if (robotFields.robotGroup == RobotSearchingGroup.GROUP_1) {
				logTestResult("WYSZUKIWANIE WEDLUG ALGORYTMU DLA GRUPY 1", true);
				wyszukanoPoPpe = wyszukajPoPpe();
				if (wyszukanoPoPpe) {
					wyszukanoNaLiscie = wyszukajNaLiscie();
					if (wyszukanoNaLiscie) {
						db.setValue("1", SCRIPT_NAME);
					} else {
						powrotDoFormatka();
						wyszukanoPoNip = wyszukajPoNipLubPesel();
						if (wyszukanoPoNip) {
							wyszukanoNaLiscie = wyszukajNaLiscie();
							if (wyszukanoNaLiscie) {
								db.setValue("1", SCRIPT_NAME);
							} else {
								db.setValue("2", SCRIPT_NAME);
							}
						} else {
							db.setValue("2", SCRIPT_NAME);
						}
					}
				} else {
					wyszukanoPoNip = wyszukajPoNipLubPesel();
					if (wyszukanoPoNip) {
						wyszukanoNaLiscie = wyszukajNaLiscie();
						if (wyszukanoNaLiscie) {
							db.setValue("1", SCRIPT_NAME);
						} else {
							db.setValue("2", SCRIPT_NAME);
						}
					} else {
						db.setValue("2", SCRIPT_NAME);
					}
				}
			} else if (robotFields.robotGroup == RobotSearchingGroup.GROUP_2) {
				logTestResult("WYSZUKIWANIE WEDLUG ALGORYTMU DLA GRUPY 2", true);
				wyszukanoPoPpe = wyszukajPoPpe();
				if (wyszukanoPoPpe) {
					wyszukanoNaLiscie = wyszukajNaLiscie();
					if (wyszukanoNaLiscie) {
						db.setValue("1", SCRIPT_NAME);
					} else {
						db.setValue("2", SCRIPT_NAME);
					}
				} else {
					db.setValue("2", SCRIPT_NAME);
				}
			} else {
				throw new RuntimeException("Nieprawidlowa grupa: " + robotFields.robotGroup
						+ ", przypisana do typu robota: " + robotFields.robotGroup);
			}
		} finally {
			if (db.getValue(SCRIPT_NAME) != null && db.getValue(SCRIPT_NAME).equals("2")) {
				zamknijbutton().click();
				anulujbutton().click();
			}
			unregisterAll();
		}
	}

	/*
	 * NIE STEROWAC STATUSEM SKRYPTU
	 */
	private void przejdzDoEwidencjiKontrahentow() {
		msMainmenuBar().click(atText("Ewidencje"));
		msMainmenuBar().click(atPath("Ewidencje->1. Ewidencja kontrahentw"));
	}

	/*
	 * NIE STEROWAC STATUSEM SKRYPTU
	 */
	private boolean wyszukajPoNipLubPesel() {

		tabControlpageTabList().click(atText("Podstawowa"));
		
		wyczybutton().click();

		if (robotFields.nip.length() > 0) {
			logTestResult("szukam po NIP " + nip, true);
			txtWartosctext().click(atPoint(20, 11));
			wyszukiwanieKontrahentwwindow().inputChars(nip);
		} else {
			if (robotFields.pesel.length() > 0) {
				logTestResult("szukam po PESEL " + robotFields.pesel, true);
				txtWartosctext3().doubleClick(atPoint(52, 12));
				wyszukiwanieKontrahentwwindow().inputKeys(robotFields.pesel);
			} else {
				throw new RuntimeException("Wartosc dla PESEL i NIP jest zerowa");
			}
		}

		okbutton().click();

		if (informacjawindow().exists()) {
			if (tbMessagetext().performTest(tbMessage_standard_2VP())) {
				okbutton2().click();
				wyszukiwanieKontrahentwwindow().click(CLOSE_BUTTON);
			}
		}

		return listaKartotekwindow().exists();
	}

	/*
	 * NIE STEROWAC STATUSEM SKRYPTU
	 */
	private boolean wyszukajPoPpe() {
		logTestResult("Wyszukiwanie po ppe: " + robotFields.ppe, true);
		tabControlpageTabList().click(atText("Dodatkowe"));
		wyczybutton().click();
		txtWartosctext2().click(atPoint(30, 8));
		wyszukiwanieKontrahentwwindow().inputChars(robotFields.ppe);
		okbutton().click();

		if (informacjawindow().exists()) {
			if (tbMessagetext().performTest(tbMessage_standard_2VP())) {
				okbutton2().click();
			}
		}

		return listaKartotekwindow().exists();
	}

	/*
	 * NIE STEROWAC STATUSEM SKRYPTU
	 */
	private boolean wyszukajNaLiscie() {

		boolean jestZnalezionyJedenKontrahent;

		logTestResult("WYSZUKIWANIE KONTRAHENTA. PODEJSCIE PIERWSZE - PODSTAWOWE", true);
		wyczyscPolaFiltru();
		wpiszDaneKontrahenta(robotFields.pelnaNazwa);

		jestZnalezionyJedenKontrahent = znalezionoTylkoJednegoKontrahenta();

		if (!jestZnalezionyJedenKontrahent) {

			jestZnalezionyJedenKontrahent = wyszukiwanieDlaPodzielonejNazwyPelnejKlienta();

			if (!jestZnalezionyJedenKontrahent) {
				wyszukiwaniePodKatemWieluKartotek();
			}
		}

		tbMaintoolBar().click(atToolTipText("Poka szczegy"), atPoint(13, 11));
		return kartotekawindow().exists();
	}

	/*
	 * NIE STEROWAC STATUSEM SKRYPTU
	 */
	private void powrotDoFormatka() {
		zamknijbutton().click();
	}

	/*
	 * NIE STEROWAC STATUSEM SKRYPTU
	 */
	private void wpiszDaneKontrahenta(String pelnaNazwa) {

		String jednostkaSprawozdawcza = robotFields.jednostkaSprawozdawcza;

		dbgMainnet().click(atPoint(42, 32));

		listaKartotekwindow().inputKeys("{TAB}");

		if (pelnaNazwa.length() > 0) {
			logTestResult("Pelna nazwa: " + pelnaNazwa, true);
			listaKartotekwindow().inputChars("%");
			listaKartotekwindow().inputKeys(pelnaNazwa);
			listaKartotekwindow().inputChars("%");
		}

		listaKartotekwindow().inputKeys("{TAB}");

		if (robotFields.kodPocztowy.length() > 0) {
			logTestResult("Kod pocztowy: " + robotFields.kodPocztowy, true);
			listaKartotekwindow().inputChars("%");
			listaKartotekwindow().inputKeys(robotFields.kodPocztowy);
			listaKartotekwindow().inputChars("%");
		}

		listaKartotekwindow().inputKeys("{TAB}");
		listaKartotekwindow().inputKeys("{TAB}");
		listaKartotekwindow().inputKeys("{TAB}");

		if (robotFields.nip.length() > 0) {
			logTestResult("Nip: " + nip, true);
			listaKartotekwindow().inputChars(nip);
		}

		listaKartotekwindow().inputKeys("{TAB}");

		if (robotFields.pesel.length() > 0) {
			logTestResult("Pesel: " + robotFields.pesel, true);
			listaKartotekwindow().inputKeys(robotFields.pesel);
		}

		listaKartotekwindow().inputKeys("{TAB}");

		if (jednostkaSprawozdawcza.equals("TS")) {
			jednostkaSprawozdawcza = "";
		}

		logTestResult("Jednostka sprawozdawcza: " + jednostkaSprawozdawcza, true);
		listaKartotekwindow().inputKeys(jednostkaSprawozdawcza);
		listaKartotekwindow().inputChars("%");

		listaKartotekwindow().inputKeys("{ExtDown}");
	}

	/*
	 * NIE STEROWAC STATUSEM SKRYPTU
	 */
	private boolean wyszukiwanieDlaPodzielonejNazwyPelnejKlienta() {

		String pelnaNazwa = robotFields.pelnaNazwa;

		logTestResult("WYSZUKIWANIE KONTRAHENTA. PODEJSCIE DRUGIE - DZIELENIE NAZWY KLIENTA WEDLUG SPACJI", true);

		List<String> pelnaNazwaAsAList = Arrays.asList(pelnaNazwa.split(" "));
		Collections.reverse(pelnaNazwaAsAList);
		for (int i = 0; i < pelnaNazwaAsAList.size(); i++) {
			pelnaNazwa = pelnaNazwaAsAList.get(i);
			wyczyscPolaFiltru();
			wpiszDaneKontrahenta(pelnaNazwa);

			if (znalezionoTylkoJednegoKontrahenta())
				return true;
		}

		return false;
	}

	/*
	 * NIE STEROWAC STATUSEM SKRYPTU
	 */
	private boolean wyszukiwaniePodKatemWieluKartotek() {

		logTestResult("WYSZUKIWANIE KONTRAHENTA. PODEJSCIE TRZECIE - POD KATEM WIELU KARTOTEK", true);

		// oblsuga dla wielu kartotek, zerujemy wartosc dla pelnej nazwy,
		// klient ma pr-nie wiele kartotek
		String pelnaNazwa = "";

		wyczyscPolaFiltru();
		wpiszDaneKontrahenta(pelnaNazwa);

		// to jest robione dla pewnosci, ze cos istnieje na kartotece
		boolean nieIstiejeZadenKlient = txStatusPanel1statusBar().performTest(txStatusPanel1_standardVP());

		if (!nieIstiejeZadenKlient) {

			FileService.usunieciePlikuPrzedZapisemKolejnego(FileService.LISTA_KONTRAHENTOW);

			tbMaintoolBar().click(atToolTipText("Eksportuj"), atPoint(33, 19));
			sleep(RobotSleep.FILE_SLEEP);
			contextpopupMenu().click(atPath("Eksport do pliku tekstowego"));
			sleep(RobotSleep.FILE_SLEEP);
			saveAswindow().inputKeys(FileService.LISTA_KONTRAHENTOW);
			sleep(RobotSleep.FILE_SLEEP);
			getScreen().click(savebutton().getScreenPoint());
			sleep(RobotSleep.FILE_SLEEP);

			int liczbaKontrahentow = sprawdzenieLiczbyKlientow();

			logTestResult("Pobrana ilosc kontrahentow: " + liczbaKontrahentow, true);

			wyczyscPolaFiltru();
			wpiszDaneKontrahenta(pelnaNazwa);

			// szukamy tylko dla jednego warunku
			for (int i = 0; i < liczbaKontrahentow; i++) {

				String umowy = (String) txtUmowytext().getProperty("Text");

				boolean umowyTest = umowy.startsWith("Kontrahent ma aktywne umowy");

				logTestResult("Umowy: " + umowy, umowyTest);

				if (umowyTest)
					return true;

				listaKartotekwindow().inputKeys("{ExtDown}");
			}

			wyczyscPolaFiltru();
			wpiszDaneKontrahenta(pelnaNazwa);

			// nie powiodl sie poprzedni test to szukamy dalej z nowym warunkiem
			for (int i = 0; i < liczbaKontrahentow; i++) {

				String umowy = (String) txtUmowytext().getProperty("Text");
				String jakoPlatnik = (String) txtPlatniktext().getProperty("Text");

				boolean umowyTest = umowy.equals("Brak aktywnych umw");
				boolean jakoPlatnikTest = jakoPlatnik.length() > 0;

				logTestResult("Umowy: " + umowy, umowyTest);
				logTestResult("Jako platnik: " + jakoPlatnik, jakoPlatnikTest);

				if (umowyTest && jakoPlatnikTest)
					return true;

				listaKartotekwindow().inputKeys("{ExtDown}");
			}

			wyczyscPolaFiltru();
			wpiszDaneKontrahenta(pelnaNazwa);

			// nie powiodl sie poprzedni test to szukamy dalej z kolejnym
			// warunkiem
			for (int i = 0; i < liczbaKontrahentow; i++) {

				String umowy = (String) txtUmowytext().getProperty("Text");
				String jakoPlatnik = (String) txtPlatniktext().getProperty("Text");
				String jakoOdbiorca = (String) txtOdbiorcatext().getProperty("Text");

				boolean umowyTest = umowy.equals("Brak aktywnych umw");
				boolean jakoPlatnikTest = jakoPlatnik.length() == 0;
				boolean jakoOdbiorcaTest = jakoOdbiorca.length() == 0;

				logTestResult("Umowy: " + umowy, umowyTest);
				logTestResult("Jako platnik: " + jakoPlatnik, jakoPlatnikTest);
				logTestResult("Jako odbiorca: " + jakoOdbiorca, jakoOdbiorcaTest);

				if (umowyTest && jakoPlatnikTest && jakoOdbiorcaTest)
					return true;

				listaKartotekwindow().inputKeys("{ExtDown}");
			}
		}
		return false;
	}

	/*
	 * NIE STEROWAC STATUSEM SKRYPTU
	 */
	private void wyczyscPolaFiltru() {
		if (tbMaintoolBar().performTest(przyciskFiltrJestWlaczonyVP())) {
			tbMaintoolBar().click(atToolTipText("Filtr"), atPoint(22, 22));
			tbMaintoolBar().click(atToolTipText("Filtr"), atPoint(22, 22));
		} else {
			tbMaintoolBar().click(atToolTipText("Filtr"), atPoint(22, 22));
		}
	}

	/*
	 * NIE STEROWAC STATUSEM SKRYPTU
	 */
	private boolean znalezionoTylkoJednegoKontrahenta() {
		if (txStatusPanel1statusBar().performTest(znalezionoTylkoJednegoKlientaVP())) {
			logTestResult("ZNALEZIONO KONTRAHENTA", true);
			return true;
		} else {
			logTestResult("NIE ZNALEZIONO KONTRAHENTA", true);
			return false;
		}
	}

	private static int sprawdzenieLiczbyKlientow() {
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(FileService.LISTA_KONTRAHENTOW))) {

			String line;
			String currentLine;

			int quantity = 0;

			while ((currentLine = bufferedReader.readLine()) != null) {
				line = currentLine.trim().replace(" ", "").replace("\t", " ").replace("  ", " ");
				String clientNumber[] = line.split(" ");
				if (clientNumber[0].matches("([0-9]{8})"))
					quantity++;
			}

			if (quantity > 0)
				return quantity;
			else
				throw new RuntimeException("Nieprawidlowa ilosc: " + quantity);

		} catch (IOException e) {
			throw new RuntimeException("Nie udany odczyt z pliku");
		}
	}
}
