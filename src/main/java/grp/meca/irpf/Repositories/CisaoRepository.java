package grp.meca.irpf.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import grp.meca.irpf.Models.Eventos.Cisao;

@Repository
public interface CisaoRepository extends JpaRepository<Cisao, Integer> {
	public List<Cisao> findAllByOrderByDataEvento();
}
