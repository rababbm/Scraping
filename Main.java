import com.opencsv.CSVWriter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//import javax.swing.text.html.parser.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 *
 * @author Rabab
 * @version 1.0
 * @since 2023-11-27
 */


public class Main {
  /**
   * Método principal que inicia el proceso de extracción de datos.
   *@param //args Argumentos de la línea de comandos (no se utilizan actualmente).
   */
  private static List<String> obtenerReparto(int indice, List<List<String>> repartos) {
    return repartos.get(indice);
  }
  public static void main(String[] args) {
    // Lista para almacenar información del reparto de cada película
    List<List<String>> repartosPeliculas = new ArrayList<>();
    // Imprimir las rutas del sistema
    System.out.println(System.getenv("PATH"));
    System.out.println(System.getenv("HOME"));

    // Configurar la propiedad del controlador de Firefox y opciones del navegador
    System.setProperty("webdriver.gecko.driver", "src/main/resources/geckodriver");
    FirefoxOptions options = new FirefoxOptions();
    options.setBinary("/home/rabab/Descargas/firefox-118.0.2/firefox/firefox");

    // Crear una instancia del controlador de Firefox (WebDriver)
    WebDriver driver = new FirefoxDriver(options);

    // Abrir la página web de IMDb
    driver.get("https://www.imdb.com/chart/top/?ref_=nv_mv_250");

    // Obtener el título de la página y mostrarlo en la consola
    String title = driver.getTitle();
    System.out.println(title);

    // Patrón para validar títulos de películas en la lista
    Pattern pattern = Pattern.compile("^[0-9]+\\.\\s.+");

    // Encontrar todos los elementos 'h3' con la clase 'ipc-title__text' (títulos de películas)
    //List<WebElement> elementosTitulo = driver.findElements(By.cssSelector("h3.ipc-title__text"));
    List<WebElement> filas = driver.findElements(By.className("ipc-metadata-list-summary-item"));

    // ArrayList para almacenar los enlaces de las películas
    ArrayList<String> enlaces = new ArrayList<String>();
    // CSV:
    List<String[]> data = new ArrayList<>();
    List<String[]> dataDirectores = new ArrayList<>();
    data.add(new String[]{"Título", "Enlace", "Año", "Duración"});
    dataDirectores.add(new String[]{"Nombre del Director"});

    // Recorrer la lista de filas y obtener los títulos, año, duración y enlace
    int contador = 0;
    for (WebElement fila : filas) {
      contador++;
      if (contador <= 100) {
        String titulo = fila.findElement(By.className("ipc-title")).findElement(By.className("ipc-title__text")).getText();
        String enlace = fila.findElement(By.className("ipc-title")).findElement(By.tagName("a")).getAttribute("href");

        enlaces.add(enlace); //cada vez q recorre que guarde los enlaces en la array

        List<WebElement> datos = fila.findElements(By.className("sc-479faa3c-8"));

        String year = "";
        String duration = "";
        for (int i = 0; i < datos.size(); i++) {
          if (i == 0) year = datos.get(i).getText();
          if (i == 1) duration = datos.get(i).getText();
        }
        System.out.println(titulo);
        System.out.println(enlace);
        System.out.println(year);
        System.out.println(duration);
        data.add(new String[]{titulo, enlace, year, duration});
      }
    }
    // Lista para CSV del reparto:
    List<String[]> dataReparto = new ArrayList<>();
    dataReparto.add(new String[]{"Enlace pelicula", "Actor principal 1", "Actor principal 2", "Actor principal 3"});
    // Itera sobre cada enlace y haz clic en él
    for (String enlace : enlaces) {
      // Abrir enlace
      driver.get(enlace);
      System.out.println("Enlace de la película: " + enlace);
      //creamo un nuevo driver con el cssselector de director
      WebElement elementoA = driver.findElement(By.cssSelector("a.ipc-metadata-list-item__list-content-item"));
      String nombreDirector = elementoA.getText();
      System.out.println("Nombre del director: " + nombreDirector);
      // Agregar información del director y reparto a la lista
      dataDirectores.add(new String[]{nombreDirector});

      //REPARTO

      WebDriverWait wait = new WebDriverWait(driver,15); //CUIDADO, cada vez q cierras cabia el cssselector de tabla, hay que modificarlo
      WebElement tabla = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".sc-69e49b85-3 > div:nth-child(1) > ul:nth-child(1) > li:nth-child(3) > div:nth-child(2)")));

      List<WebElement> lista = tabla.findElements(By.tagName("li"));
      List<String> repartosPelicula = new ArrayList<>();  // Cambiado el nombre de la lista


      for (WebElement li : lista) {
        repartosPelicula.add(li.findElement(By.tagName("a")).getText());
        System.out.println(li.findElement(By.tagName("a")).getText());
      }
      // Agrega el enlace a cada fila del CSV del reparto
      repartosPelicula.add(0, enlace);
      dataReparto.add(repartosPelicula.toArray(new String[0]));
      repartosPeliculas.add(repartosPelicula);

    }

    // CSV
    try (CSVWriter writer = new CSVWriter(new FileWriter("/home/rabab/DAM2/Acces a dades/Peliculas/peliculas.csv"))) {
      writer.writeAll(data);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try (CSVWriter writer = new CSVWriter(new FileWriter("/home/rabab/DAM2/Acces a dades/Peliculas/directores.csv"))) {
      writer.writeAll(dataDirectores);
    } catch (IOException e) {
      e.printStackTrace();
    }
    try (CSVWriter writer = new CSVWriter(new FileWriter("/home/rabab/DAM2/Acces a dades/Peliculas/reparto.csv"))) {
      writer.writeAll(dataReparto);
    } catch (IOException e) {
      e.printStackTrace();
    }
    driver.quit();
    // XML
    // Crear un documento XML
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.newDocument();

      // Elemento raíz
      Element rootElement = doc.createElement("peliculas");
      doc.appendChild(rootElement);

      // Iterar sobre las películas y añadir elementos al XML
      for (int i = 1; i < data.size(); i++) {
        Element peliculaElement = doc.createElement("pelicula");
        rootElement.appendChild(peliculaElement);

        Element tituloElement = doc.createElement("titulo");
        tituloElement.appendChild(doc.createTextNode(data.get(i)[0]));
        peliculaElement.appendChild(tituloElement);

        Element enlaceElement = doc.createElement("enlace");
        enlaceElement.appendChild(doc.createTextNode(data.get(i)[1]));
        peliculaElement.appendChild(enlaceElement);

        Element yearElement = doc.createElement("year");
        yearElement.appendChild(doc.createTextNode(data.get(i)[2]));
        peliculaElement.appendChild(yearElement);

        Element durationElement = doc.createElement("duration");
        durationElement.appendChild(doc.createTextNode(data.get(i)[3]));
        peliculaElement.appendChild(durationElement);

        // Añadir información del director al XML
        Element directorElement = doc.createElement("director");
        directorElement.appendChild(doc.createTextNode(dataDirectores.get(i)[0]));
        peliculaElement.appendChild(directorElement);

        // Añadir información del reparto al XML
        Element repartoElement = doc.createElement("reparto");

        // Obtener información del reparto de la lista repartosPeliculas
        List<String> repartos = repartosPeliculas.get(i - 1);

        // Agregar elementos de actor al elemento de reparto
        for (String actor : repartos) {
          Element actorElement = doc.createElement("actor");
          actorElement.appendChild(doc.createTextNode(actor));
          repartoElement.appendChild(actorElement);
        }

        // Adjuntar el elemento de reparto a la estructura de la película en el XML
        peliculaElement.appendChild(repartoElement);
      }


      // Guardar el documento XML en un archivo
      File xmlFile = new File("/home/rabab/DAM2/Acces a dades/Peliculas/peliculas.xml");
      javax.xml.transform.TransformerFactory transformerFactory = javax.xml.transform.TransformerFactory.newInstance();
      javax.xml.transform.Transformer transformer = transformerFactory.newTransformer();

      transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
      javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(doc);
      javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(xmlFile);
      transformer.transform(source, result);

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

}
