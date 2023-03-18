package example.micronaut;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;

import java.util.Optional;

@Repository
public interface ReguserRepository extends CrudRepository<Reguser, Long> {

    Optional<Reguser> findByUsernameIgnoreCase(String username);

}
