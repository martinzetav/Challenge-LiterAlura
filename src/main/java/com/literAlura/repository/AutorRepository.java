package com.literAlura.repository;

import com.literAlura.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    Optional<Autor> findByNombre(String nombre);
    @Query("SELECT a FROM Autor a JOIN FETCH a.libros")
    List<Autor> findAllAutoresConLibros();
    @Query("SELECT a FROM Autor a JOIN FETCH a.libros WHERE a.fechaNac <= :anio AND (a.fechaFallecimiento IS NULL OR a.fechaFallecimiento >= :anio)")
    List<Autor> findAutoresVivosConLibros(@Param("anio") String anio);

}
