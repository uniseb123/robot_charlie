import resources.UruchomienieAplikacjiHelper;
import tools.Application;
import tools.DB;
import tools.FileService;
import tools.RobotSleep;
import tools.RobotType;

import com.rational.test.ft.*;
import com.rational.test.ft.object.interfaces.*;
import com.rational.test.ft.object.interfaces.SAP.*;
import com.rational.test.ft.object.interfaces.WPF.*;
import com.rational.test.ft.object.interfaces.dojo.*;
import com.rational.test.ft.object.interfaces.siebel.*;
import com.rational.test.ft.object.interfaces.flex.*;
import com.rational.test.ft.object.interfaces.generichtmlsubdomain.*;
import com.rational.test.ft.script.*;
import com.rational.test.ft.value.*;
import com.rational.test.ft.vp.*;
import com.ibm.rational.test.ft.object.interfaces.sapwebportal.*;

public class UruchomienieAplikacji extends UruchomienieAplikacjiHelper {

	private DB db;
	private RobotType robotType;
	private String app;

	public void testMain(Object[] args) {

		db = DB.getInstance(args[0]);
		app = (String) args[1];

		robotType = RobotType.getRobotType(db);

		robotType = RobotType.getRobotType(db);

		if (robotType == RobotType.JB) {
			String dataPrzesuniecia = db.getValue(231);
			if (dataPrzesuniecia != null) {
				if (!dataPrzesuniecia.trim().equals("")) {
					db.setValue("-1", this.getClass().getName());
					db.update("Data obowiazywania umowy jest przesunieta  brak kontynuacji umowy");
					stop();
				}
			}
		}
		if (robotType == RobotType.JURIJ1) {
			String taryfa_produkt_sprzedazy = db.getValue(250);
			String taryfa_dystrybucyjna = db.getValue(251);

			if (!taryfa_produkt_sprzedazy.equals(taryfa_dystrybucyjna)) {
				db.setValue("-1", this.getClass().getName());
				db.update("Rnica grupy Taryfowej w danych wejciowych midzy OSD a Sprzedawc");
				stop();
			}
		}
		startApp(app);
		sleep(RobotSleep.RUN_SLEEP);

		if (app.equals(Application.SKOK_O_TEST)) {
			sleep(RobotSleep.RUN_SLEEP);
			uwagawindow2().inputKeys("{TAB}");
			sleep(RobotSleep.RUN_SLEEP);
			uwagawindow2().inputKeys("{ENTER}");
			sleep(RobotSleep.RUN_SLEEP);
		} else if (app.equals(Application.SKOK_O_MIG)) {
			sleep(RobotSleep.RUN_SLEEP);
			uwagawindow3().inputKeys("{TAB}");
			sleep(RobotSleep.RUN_SLEEP);
			uwagawindow3().inputKeys("{ENTER}");
			sleep(RobotSleep.RUN_SLEEP);
		}

		if (robotType == RobotType.JURIJ1 || robotType == RobotType.KRYSTYNA7) {

			String numerKontrahenta = db.getValue(316);

			if (numerKontrahenta != null) {

				if (!numerKontrahenta.equals("")) {

					boolean numerKontrahentaJestPrawidlowy = sprawdzenieNumeruKontrahenta(numerKontrahenta);

					if (numerKontrahentaJestPrawidlowy) {
						logowanieDoObuRaportow("Numer ewidencyjny jest poprawny");
						db.setValue(numerKontrahenta, DB.NUMER_KONTRAHENTA);
						db.setValue("2", this.getClass().getName());
						db.setValue(Boolean.TRUE.toString(), DB.TERMIN_PLATNOSCI_ZGODNY);
					} else {
						logowanieDoObuRaportow("Bledny numer kontrahenta: " + numerKontrahenta);
						db.setValue("-1", this.getClass().getName());
						stop();
					}

				} else {
					logowanieDoObuRaportow("Wartosc numeru ewidencyjnego w danych wejsciowych jest rowna \"\"");
					db.setValue("1", this.getClass().getName());
				}

			} else {
				logowanieDoObuRaportow("Wartosc numeru ewidencyjnego w danych wejsciowych jest rowna null");
				db.setValue("1", this.getClass().getName());
			}

		} else {
			db.setValue("1", this.getClass().getName());
		}
	}

	private boolean sprawdzenieNumeruKontrahenta(String clientNumber) {

		logowanieDoObuRaportow("Pobrany numer ewidencyjny ENEA centrum: " + clientNumber);

		boolean numerKontrahentaZaczynaSieOdCyfry2 = clientNumber.startsWith("2");
		boolean dlugoscNumeruKontrahentaWynosi8 = clientNumber.length() == 8;

		logowanieDoObuRaportow("Numer kontrahenta zaczyna sie od cyfry 2: " + clientNumber + " : "
				+ numerKontrahentaZaczynaSieOdCyfry2);
		logowanieDoObuRaportow(
				"Dlugosc numeru kontrahenta wynosi 8: " + clientNumber + " : " + dlugoscNumeruKontrahentaWynosi8);

		return dlugoscNumeruKontrahentaWynosi8 && numerKontrahentaZaczynaSieOdCyfry2;
	}

	private void logowanieDoObuRaportow(String log) {
		db.update(log);
		logInfo(log);
	}
}
