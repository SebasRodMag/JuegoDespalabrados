
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 *
 * @author sebas
 */
public class Despalabrados {

    private static final int RONDAS = 12;
    private static final int LETRAS_POR_RONDA = 12;
    private static final String DIR_LISTA_PALABRAS = System.getProperty("user.dir") + "/recursos/listaPalabras.txt";
    private static final String DIR_LISTA_PUNTOS = System.getProperty("user.dir") + "/recursos/Puntos.txt";
    //En el string de letras, se repiten algunas de ellas para aumentar la posibilidad de que se repitan
    //Segun un algoritmo realizado para contabilizar las letras que se repiten en las palabras del idioma español
    //Es mas facil formar una palabra cuando las bocales y algunas consonantes se repiten.
    private static final String LETRAS = "AAAABBCCDDEEEEFFGGHHIIIIJJKLLLLMMMNNNÑÑÑÑOOOOPQRRRRSSTTTUUUUVVWXYZ";
    private static final List<String> LISTA_PALABRAS = new ArrayList<>();
    private static final List<String> LISTA_USUARIOS = new ArrayList<>();
    private static final List<String> LISTA_GANADORES = new ArrayList<>();
    private static final Map<String, Integer> PUNTOS_USUARIO = new HashMap<>();

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        Random aleatorio = new Random();
        int numJugadores = 0;
        int puntosAcumulados = 0;
        boolean numValido = false;
        
        try(BufferedReader ganadores = new BufferedReader(new FileReader(DIR_LISTA_PUNTOS))){
            String linea;
            while ((linea = ganadores.readLine()) != null) {
                LISTA_GANADORES.add(linea);
            }
        }catch (FileNotFoundException e) {
            System.out.println("Archivo no encontrado: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de lectura: " + e.getMessage());
        }

        //Iniciamos la lectura del fichero y el bufer para que lea linea a linea.
        try (BufferedReader entrada = new BufferedReader(new FileReader(DIR_LISTA_PALABRAS))) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                //agregamos cada linea leida a la lista a LISTA_PALABRAS.
                LISTA_PALABRAS.add(linea.toUpperCase());
            }
            //Controlamos la excepciones que se puedan provocar
        } catch (FileNotFoundException e) {
            System.out.println("Archivo no encontrado: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de lectura: " + e.getMessage());
        }

        //Solicitar numero de jugadores
        do {
            try {
                System.out.print("Ingrese el número de jugadores (1-6): ");
                numJugadores = teclado.nextInt();
                teclado.nextLine(); // Consumir el salto de línea
                if (numJugadores >= 1 && numJugadores <= 6) {
                    numValido = true;
                } else {
                    System.out.println("El número ingresado está fuera del rango permitido.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Debes introducir un número del 1 al 6");
                teclado.nextLine(); // Limpiar el buffer
            }
        } while (!numValido);

        // Crear una lista para almacenar los nombres y las puntuaciones de los jugadores
        //Tiene que leer la lista de jugadores y las puntuaciones desde un archivo. Y si el jugador no esta, crear uno nuevo
        //Hay que darle una vuelta a esto.
        try (BufferedReader entrada = new BufferedReader(new FileReader(DIR_LISTA_PUNTOS))) {
            String[] linea;
            while (!(entrada.readLine() != null)) {
                linea = entrada.readLine().split(":");
                PUNTOS_USUARIO.put(linea[0], Integer.valueOf(linea[1]));
            }
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de puntuaciones no encontrado. Se creará uno nuevo.");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        //Solicitar el i de los jugadores
        for (int i = 0; i < numJugadores; i++) {
            boolean correcto = false;
            while (!correcto) {
                System.out.print("Ingrese el nombre del jugador " + (i + 1) + ": ");
                String nombreJugador = teclado.nextLine().toUpperCase();
                System.out.print("El nombre seleccionado es " + nombreJugador + ". ¿Es correcto? S/N: ");
                String nombreCorrecto = teclado.nextLine().toUpperCase();
                // Comprobamos que el nombre introducido sea el deseado, si no es así, se vuelve a introducir.
                if (nombreCorrecto.equalsIgnoreCase("S")) {
                    LISTA_USUARIOS.add(nombreJugador);
                    PUNTOS_USUARIO.putIfAbsent(nombreJugador, 0);
                    correcto = true;
                }
            }
        }

        for (int ronda = 1; ronda <= RONDAS; ronda++) {
            System.out.println("\n| RONDA " + ronda + " |");
            System.out.println("------------");
            List<Character> letras = generarLetraAleatoria(aleatorio);
            System.out.println("Te han tocado las siguientes Letras: ");
            System.out.println("|" + letras + "|");

            //Generamos las rondas por jugador
            for (int i = 0; i < numJugadores; i++) {
                System.out.println("\nEs el turno de " + LISTA_USUARIOS.get(i));
                System.out.print("Escribe una palabra a partir de la letras recibidas: ");
                String palabraSugerida = teclado.nextLine().toUpperCase();

                //Comprobamos que se utilizan las letras correctas
                if (verificarLetras(palabraSugerida, letras)) {
                    //Comprovamos que sea una palabra de la lista de palabras correctas
                    if (palabraValida(palabraSugerida, LISTA_PALABRAS)) {
                        if (LISTA_PALABRAS.contains(palabraSugerida)) {
                            //Si es asi, se asignan puntos
                            int puntos = calcularPuntos(palabraSugerida);
                            System.out.println("Correcto! Has conseguido " + puntos + " puntos.");
                            puntosAcumulados = PUNTOS_USUARIO.get(LISTA_USUARIOS.get(i)) + puntos;
                            PUNTOS_USUARIO.put(LISTA_USUARIOS.get(i), puntosAcumulados);
                        }
                    } else {
                        //si no
                        int puntos = 0;
                        System.out.println("Incorrecto. No es una palabra valida.");
                        puntosAcumulados = PUNTOS_USUARIO.get(LISTA_USUARIOS.get(i)) + puntos;
                        PUNTOS_USUARIO.put(LISTA_USUARIOS.get(i), puntosAcumulados);
                    }
                } else {
                    System.out.println("Incorrecto. La palabra contiene letras no que no estan en la lista");
                }
                System.out.println("Puntuación total de " + LISTA_USUARIOS.get(i) + ": " + puntosAcumulados);
                //PUNTOS_USUARIO.replace(LISTA_USUARIOS.get(i), PUNTOS_USUARIO.get(LISTA_USUARIOS.get(i)));

            }

        }

        //Buscar al ganador
        int puntuacionMaxima = Collections.max(PUNTOS_USUARIO.values());
        List<String> ganadores = new ArrayList<>();
        for (Map.Entry<String, Integer> listajugadores : PUNTOS_USUARIO.entrySet()) {
            if (listajugadores.getValue() == puntuacionMaxima) {
                ganadores.add(listajugadores.getKey());
            }
        }


        System.out.println("\nFin de la partida.");
        System.out.println("------------------");
        int contador = 1;
        for (Map.Entry<String, Integer> lista : PUNTOS_USUARIO.entrySet()) {
                System.out.println(contador + ": " + lista.getKey() + " -> " + lista.getValue() + " puntos.");
                contador++;
            }

            if (ganadores.size() == 1) {
                System.out.println("El ganador es " + ganadores.get(0) + " con " + puntuacionMaxima + " puntos.");
                LISTA_GANADORES.add(ganadores.get(0)+":"+puntuacionMaxima);
            } else {
                System.out.println("Hay un empate etre los jugadores: " + String.join(", ", ganadores) + " con " + puntuacionMaxima + " puntos.");
                LISTA_GANADORES.add(ganadores.get(0)+":"+puntuacionMaxima);
                LISTA_GANADORES.add(ganadores.get(1)+":"+puntuacionMaxima);
            }

        //Guardar la informacion de lo sganadores en un archivo.
        try (PrintWriter salida = new PrintWriter(new FileWriter(DIR_LISTA_PUNTOS))) {
            for(String ganador : LISTA_GANADORES){
                salida.print(ganador+"\n");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        System.out.println();
        System.out.println("| LISTA DE GANADORES |");
        System.out.println("----------------------");
        System.out.println();
        System.out.println("NOMBRE.     PUNTOS.");
        System.out.println("-------     -------");
        for(String ganador : LISTA_GANADORES){
                String[] salida;
                salida = ganador.split(":");
                System.out.printf("%-12s %s",salida[0],salida[1]+"\n");
            }

    }
    

    private static List<Character> generarLetraAleatoria(Random random) {
        List<Character> letras = new ArrayList<>();
        for (int i = 0; i < LETRAS_POR_RONDA; i++) {
            letras.add(LETRAS.charAt(random.nextInt(LETRAS.length())));
        }
        return letras;
    }

    private static boolean verificarLetras(String palabra, List<Character> letras) {
        boolean valida= true;
        List<Character> letrasDisponibles = new ArrayList<>(letras);
        for (char letra : palabra.toCharArray()) {
            if (!letrasDisponibles.remove((Character) letra)) {
                valida = false;
            }
        }
        
        return valida;
    }

    private static boolean palabraValida(String palabra, List<String> palabras) {
        return palabras.contains(palabra);
    }

    private static int calcularPuntos(String palabra) {
        int totalPuntos = 0;
        for (int i = 0; i < palabra.length(); i++) {
            switch (palabra.toUpperCase().charAt(i)) {
                case 'A', 'E', 'O', 'I', 'S', 'N', 'L', 'R', 'U', 'T' ->
                    totalPuntos++;
                case 'D', 'G' ->
                    totalPuntos += 2;
                case 'C', 'B', 'M', 'P' ->
                    totalPuntos += 3;
                case 'H', 'F', 'V', 'Y' ->
                    totalPuntos += 4;
                case 'K', 'Q' ->
                    totalPuntos += 5;
                case 'J', 'Ñ', 'W', 'X' ->
                    totalPuntos += 8;
                case 'Z' ->
                    totalPuntos += 10;

            }
        }
        return totalPuntos;
    }
}
