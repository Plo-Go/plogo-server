package plogo.plogoserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import plogo.plogoserver.domain.LogPhoto;

public interface LogPhotoRepository extends JpaRepository<LogPhoto, Long> {


}
