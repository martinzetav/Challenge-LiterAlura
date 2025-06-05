package com.literAlura.service;

import com.literAlura.model.Autor;
import com.literAlura.model.Datos;
import com.literAlura.model.DatosLibro;
import com.literAlura.model.Libro;
import com.literAlura.repository.LibroRepository;
import com.literAlura.utils.ConsumoApi;
import com.literAlura.utils.ConvierteDatos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Challenge {

    private ConsumoApi consumoApi = new ConsumoApi();
    private ConvierteDatos convierteDatos = new ConvierteDatos();
    private final String URL_BASE = "https://gutendex.com/books/";
    private Scanner sc = new Scanner(System.in);
    private LibroRepository repositorio;

    public Challenge(LibroRepository libroRepository) {
        this.repositorio = libroRepository;
    }

    public void menu(){
        int opcion = -1;
        while(opcion != 0){
            opcion = mostrarMenu();
            switch (opcion){
                case 1:
                    guardarLibroBuscado();
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

        List<Autor> autores = new ArrayList<>();

        Libro libro = Libro.builder()
                .titulo(datosLibro.titulo())
                .idioma(datosLibro.idiomas().get(0))
                .numeroDescargas(datosLibro.numeroDescargas())
                .build();

        datosLibro.autores().stream()
                .forEach(a -> autores.add(Autor.builder()
                        .nombre(a.nombre())
                        .fechaNac(a.fechaNac())
                        .libro(libro)
                        .build()));

        libro.setAutores(autores);

        repositorio.save(libro);
        System.out.println("----- LIBRO -----");
                        System.out.println("Titulo: " + libro.getTitulo());
                        if(autores.size() > 1){
                            System.out.println("Autores: ");
                            autores.forEach(a -> {
                                System.out.println("        " + a.getNombre());
                            });
                        } else {
                            System.out.println("Autor: " + autores.get(0).getNombre());
                        }
                        System.out.println("Idioma: " + libro.getIdioma());
                        System.out.println("Numero de descargas: " + libro.getNumeroDescargas());
                        System.out.println("-----------");
    }


}
