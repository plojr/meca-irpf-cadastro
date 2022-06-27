package grp.meca.irpf.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import grp.meca.irpf.Models.Fusao;

@Repository
public interface FusaoRepository extends JpaRepository<Fusao, Integer> {

}