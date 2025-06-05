package com.literAlura.service;

import com.literAlura.model.Autor;
import com.literAlura.model.Datos;
import com.literAlura.model.DatosLibro;
import com.literAlura.model.Libro;
import com.literAlura.repository.AutorRepository;
import com.literAlura.repository.LibroRepository;
import com.literAlura.utils.ConsumoApi;
import com.literAlura.utils.ConvierteDatos;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Challenge {

    private ConsumoApi consumoApi = new ConsumoApi();
    private ConvierteDatos convierteDatos = new ConvierteDatos();
    private final String URL_BASE = "https://gutendex.com/books/";
    private Scanner sc = new Scanner(System.in);
    private LibroRepository libroRepository;
    private AutorRepository autorRepository;
    public Challenge(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public void menu(){
        int opcion = -1;
        while(opcion != 0){
            opcion = mostrarMenu();
            switch (opcion){
                case 1:
                    guardarLibroBuscado();
                    break;
                case 2:
                    getLibrosRegistrados();
                    break;
                case 3:
                    getAutoresRegistrados();
                    break;
            }
        }
    }

    private int mostrarMenu(){
        System.out.println("""
                ------------------------
                Elija la opción a través de su número:
                1- Buscar libro por titulo.
                2- Listar libros registrados.
                3- Listar autores registrados.
                4- Listar autores vivos en un determinado año.
                5- Listar libros por idioma.
                0- Salir.
                """);

        return sc.nextInt();
    }

    private DatosLibro getDatosLibro(){
        System.out.println("Ingrese el nombre del libro que desea buscar:");
        sc.nextLine();
        String tituloLibro = sc.nextLine();
        String json = consumoApi.obtenerDatos(URL_BASE+"?search="+tituloLibro.replace(" ", "+"));
        Datos datosBusqueda = convierteDatos.obtenerDatos(json, Datos.class);
        Optional<DatosLibro> libroBuscado = datosBusqueda.libros().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();
        if(libroBuscado.isPresent()){
            return libroBuscado.get();
        } else {
            System.out.println("No se encontro el libro con el nombre " + tituloLibro.toUpperCase());
            return null;
        }
    }

    private void guardarLibroBuscado(){
        DatosLibro datosLibro = getDatosLibro();
        if(datosLibro == null) return;
        Optional<Libro> libroExistente = libroRepository.findByTitulo(datosLibro.titulo());
        if(libroExistente.isPresent()){
            System.out.println("El libro " + libroExistente.get().getTitulo() + " ya existe.");
            return;
        }
        Optional<Autor> autorExistente = autorRepository.findByNombre(datosLibro.autores().get(0).nombre());
        Autor autor;
        if(autorExistente.isPresent()){
            autor = autorExistente.get();
        } else {
            autor = Autor.builder()
                    .nombre(datosLibro.autores().get(0).nombre())
                    .fechaNac(datosLibro.autores().get(0).fechaNac())
                    .fechaFallecimiento(datosLibro.autores().get(0).fechaFallecimiento())
                    .build();

            autor = autorRepository.save(autor);
        }


        Libro libro = Libro.builder()
                .titulo(datosLibro.titulo())
                .idioma(datosLibro.idiomas().get(0))
                .numeroDescargas(datosLibro.numeroDescargas())
                .autor(autor)
                .build();

        libroRepository.save(libro);

        imprimirLibro(libro);
    }

    private void getLibrosRegistrados(){
        List<Libro> libros = libroRepository.findAll();
        libros.forEach(this::imprimirLibro);
    }

    private void getAutoresRegistrados(){
        List<Autor> autores = autorRepository.findAllAutoresConLibros();
        autores.forEach(this::imprimirAutor);
    }

    private void imprimirLibro(Libro libro){
        System.out.println("----- LIBRO -----");
        System.out.println("Titulo: " + libro.getTitulo());
        System.out.println("Autor: " + libro.getAutor().getNombre());
        System.out.println("Idioma: " + libro.getIdioma());
        System.out.println("Numero de descargas: " + libro.getNumeroDescargas());
        System.out.println("-----------");
        System.out.println("");
    }

    private void imprimirAutor(Autor autor){
        List<String> libros = autor.getLibros().stream()
                .map(Libro::getTitulo)
                .toList();
        System.out.println("Nombre: " + autor.getNombre());
        System.out.println("Fecha de nacimiento: " + autor.getFechaNac());
        System.out.println("Fecha de fallecimiento: " + autor.getFechaFallecimiento());
        System.out.println("Libros: " + libros);
    }


}
