import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class DemoWaitFor {

    public static void main(String[] args) {
        System.out.println("[PARE] Iniciant el programa principal...");

        // 1. Definim el procés fill (Segons el SO utilitzem una comanda amb retard)
        ProcessBuilder pb;
        boolean esWindows = System.getProperty("os.name").toLowerCase().contains("win");

        if (esWindows) {
            // En Windows fem servir 'timeout' per simular una tasca de 3 segons
            pb = new ProcessBuilder("cmd.exe", "/c", "timeout /t 3 /nobreak > nul");
        } else {
            // En Linux/macOS fem servir 'sleep' durant 3 segons
            pb = new ProcessBuilder("sleep", "3");
        }

        try {
            System.out.println("[PARE] Llançant el procés fill...");
            long tempsInici = System.currentTimeMillis();

            // Iniciem el procés (No bloqueja, s'executa en segon pla)
            Process procésFill = pb.start();

            System.out.println("[PARE] El procés fill s'està executant en segon pla.");
            System.out.println("[PARE] Crido a waitFor() i em quedo ESPERANT...");

            // 2. Bloquejem el procés pare fins que el fill acabi
            int exitCode = procésFill.waitFor();
            //int exitCode = 0; //Quan comentam la linia de waitfor veure que passa i descomentar aquesta linia
            long tempsFinal = System.currentTimeMillis();
            long duradaTotal = (tempsFinal - tempsInici) / 1000;

            // 3. El pare es desbloqueja quan el fill finalitza
            System.out.println("\n------------------------------------------------");
            System.out.println("[PARE] El procés fill HA FINALITZAT!");
            System.out.println("[PARE] Temps d'espera total: " + duradaTotal + " segons.");
            System.out.println("[PARE] Codi de sortida (Exit Value): " + exitCode);

            if (exitCode == 0) {
                System.out.println("[PARE] Estat: Execució Exitosa (0).");
            } else {
                System.out.println("[PARE] Estat: Error en el procés fill (" + exitCode + ").");
            }
            System.out.println("------------------------------------------------\n");

            // --- BONA PRÀCTICA: Demostració de waitFor() amb Timeout ---
            System.out.println("[PARE] Prova addicional: waitFor amb Timeout (màxim 1 segon)...");
            Process procésLent = esWindows ?
                    new ProcessBuilder("cmd.exe", "/c", "timeout /t 5 /nobreak > nul").start() :
                    new ProcessBuilder("sleep", "5").start();

            // Esperem un màxim d'1 segon. Retorna 'true' si acaba a temps, 'false' si salta el timeout.
            boolean finalitzatATemps = procésLent.waitFor(1, TimeUnit.SECONDS);

            if (!finalitzatATemps) {
                System.out.println("[PARE] ALERTA: El procés ha superat el temps límit! Forçant el tancament...");
                procésLent.destroy(); // Forcem el tancament del procés (SIGKILL / SIGTERM)
                System.out.println("[PARE] Procés destructurat amb destroy().");
            }

        } catch (IOException e) {
            System.err.println("[PARE] Error en iniciar el procés: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("[PARE] El fil pare ha estat interromput mentre esperava: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restablim l'estat d'interrupció
        }

        System.out.println("[PARE] Programa principal finalitzat.");
    }
}