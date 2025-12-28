package de.desarrollospy.side.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class NumberToLetterConverter {

    private static final Map<Integer, String> v_cientos = new HashMap<>();
    private static final Map<Integer, String> v_decimos = new HashMap<>();
    private static final Map<Integer, String> v_digitos = new HashMap<>();

    static {
        cargarMapa();
    }

    public static String convertNumberToLetter(BigDecimal numero) {
        if (numero == null) return "Cero";
        
        String t_letras = "";
        String t_aux = "";

        // Escalar a 2 decimales
        BigDecimal numeroEscalado = numero.setScale(2, RoundingMode.HALF_UP);
        
        // Convertir a string plano (ej: 1000000.00 -> 100000000)
        String plainStr = numeroEscalado.toPlainString().replace(".", "").replace(",", "");
        
        // Rellenar a 15 caracteres. 
        // Estructura esperada: TTT MMM mmm UUU DD (Trillón, Millón, Mil, Unidad, Decimal)
        // Indices:             012 345 678 901 23
        // Pero usamos pad 15:  0   123 456 789 012 34
        String t_num = padLeft(plainStr, 15, '0');

        // --- Billones (Indices 1, 2, 3) ---
        if (!t_num.substring(1, 4).equals("000")) {
            t_letras = t_letras + fn_cientos(t_num.substring(1, 4));
            if (t_num.substring(3, 4).equals("1")
                    && !(t_num.substring(2, 3).equals("1"))
                    || t_num.substring(2, 4).equals("00")) {
                t_letras = t_letras.substring(0, t_letras.length() - 1);
            }
            t_letras = t_letras + " Mil ";
        }

        // --- Millones (Indices 4, 5, 6) ---
        if (!t_num.substring(4, 7).equals("000")) {
            t_letras = t_letras + fn_cientos(t_num.substring(4, 7));
            
            // Corrige "Uno" a "Un" si termina en 1 (ej: Un Millón)
            if (t_num.substring(6, 7).equals("1")
                    && !(t_num.substring(5, 6).equals("1"))) {
                t_letras = t_letras.substring(0, t_letras.length() - 1);
            }
            
            // Singular o Plural
            if (t_num.substring(4, 7).equals("001")
                    && t_num.substring(1, 4).equals("000")) {
                t_letras = t_letras + " Millón ";
            } else {
                t_letras = t_letras + " Millones ";
            }
        } else {
            // Si no hay millones pero sí billones, agregar "Millones"
            if (!t_num.substring(1, 4).equals("000")) {
                t_letras = t_letras + " Millones ";
            }
        }

        // --- Miles (Indices 7, 8, 9) ---
        if (!t_num.substring(7, 10).equals("000")) {
            t_letras = t_letras + fn_cientos(t_num.substring(7, 10));
            // Corrige "Uno" a "Un" (ej: Un Mil)
            if (t_num.substring(9, 10).equals("1")
                    && !(t_num.substring(8, 9).equals("1"))
                    || t_num.substring(8, 10).equals("00")) {
                t_letras = t_letras.substring(0, t_letras.length() - 1);
            }
            t_letras = t_letras + " Mil ";
        }

        // --- Cientos/Unidades (Indices 10, 11, 12) ---
        if (!t_num.substring(10, 13).equals("000")) {
            t_letras = t_letras + fn_cientos(t_num.substring(10, 13));
        }

        // --- Decimales (Indices 13, 14) ---
        if (!t_num.substring(13, 15).equals("00")) { 
            t_aux = "0" + t_num.substring(13, 15);
            // fn_cientos espera 3 dígitos, enviamos 0XX
            t_letras = t_letras + " con " + fn_cientos(t_aux);
            t_letras = t_letras + " Guaranies";
        }
        
        t_letras = t_letras + ".-";
        // Limpieza final de espacios dobles
        return t_letras.replaceAll("\\s+", " ").trim();
    }

    private static String fn_cientos(String pr_num) {
        String pr_cadena = "";
        
        // Centenas
        if (!pr_num.substring(0, 1).equals("0")) {
            if (pr_num.substring(0, 1).equals("1")) {
                pr_cadena = pr_cadena + "Cien";
                if (!(pr_num.substring(1, 2).equals("0"))
                        || !(pr_num.substring(2, 3).equals("0"))) {
                    pr_cadena = pr_cadena + "to";
                }
                pr_cadena = pr_cadena + " ";
            } else {
                pr_cadena = pr_cadena
                        + v_cientos.get(Integer.valueOf(pr_num.substring(0, 1))) + "ientos ";
            }
        }

        // Decenas
        if (!pr_num.substring(1, 2).equals("0")) {
            if (pr_num.substring(1, 2).equals("1")
                    && Integer.parseInt(pr_num.substring(2, 3)) < 6) {
                pr_cadena = pr_cadena
                        + v_digitos.get(Integer.parseInt(pr_num.substring(2, 3)) + 10);
            } else {
                pr_cadena = pr_cadena
                        + v_decimos.get(Integer.valueOf(pr_num.substring(1, 2))) + " ";
                if (!pr_num.substring(2, 3).equals("0"))
                    pr_cadena = pr_cadena + "y ";
            }
        }

        // Unidades
        if (!pr_num.substring(2, 3).equals("0")) {
            if (!(pr_num.substring(1, 2).equals("1"))
                    || (pr_num.substring(1, 2).equals("1") && Integer.parseInt(pr_num.substring(2, 3)) > 5)) {
                pr_cadena = pr_cadena
                        + v_digitos.get(Integer.valueOf(pr_num.substring(2, 3)));
            }
        }
        return pr_cadena;
    }

    private static void cargarMapa() {
        v_cientos.put(1, "C");
        v_cientos.put(2, "Dosc");
        v_cientos.put(3, "Trec");
        v_cientos.put(4, "Cuatroc");
        v_cientos.put(5, "Quin");
        v_cientos.put(6, "Seisc");
        v_cientos.put(7, "Setec");
        v_cientos.put(8, "Ochoc");
        v_cientos.put(9, "Novec");
        
        v_decimos.put(1, "Diez");
        v_decimos.put(2, "Veinte");
        v_decimos.put(3, "Treinta");
        v_decimos.put(4, "Cuarenta");
        v_decimos.put(5, "Cincuenta");
        v_decimos.put(6, "Sesenta");
        v_decimos.put(7, "Setenta");
        v_decimos.put(8, "Ochenta");
        v_decimos.put(9, "Noventa");
        
        v_digitos.put(1, "Uno");
        v_digitos.put(2, "Dos");
        v_digitos.put(3, "Tres");
        v_digitos.put(4, "Cuatro");
        v_digitos.put(5, "Cinco");
        v_digitos.put(6, "Seis");
        v_digitos.put(7, "Siete");
        v_digitos.put(8, "Ocho");
        v_digitos.put(9, "Nueve");
        v_digitos.put(10, "Diez");
        v_digitos.put(11, "Once");
        v_digitos.put(12, "Doce");
        v_digitos.put(13, "Trece");
        v_digitos.put(14, "Catorce");
        v_digitos.put(15, "Quince");
    }

    private static String padLeft(String input, int length, char padChar) {
        if (input.length() >= length) {
            return input;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - input.length(); i++) {
            sb.append(padChar);
        }
        sb.append(input);
        return sb.toString();
    }
}