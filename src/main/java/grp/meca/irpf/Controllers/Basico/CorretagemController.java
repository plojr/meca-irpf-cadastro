package grp.meca.irpf.Controllers.Basico;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import grp.meca.irpf.Controllers.ErroController;
import grp.meca.irpf.Models.Basico.NotaDeCorretagem;
import grp.meca.irpf.Models.Basico.Ordem;
import grp.meca.irpf.Models.Basico.Ticker;
import grp.meca.irpf.Repositories.NotaDeCorretagemRepository;
import grp.meca.irpf.Repositories.OrdemRepository;
import grp.meca.irpf.Repositories.TickerRepository;

@Controller
@RequestMapping("/basico")
public class CorretagemController {
	
	@Autowired
	private TickerRepository tickerRepository;
	
	@Autowired
	private OrdemRepository ordemRepository;
	
	@Autowired
	private NotaDeCorretagemRepository corretagemRepository;
	
	@GetMapping("/corretagem")
	public String adicionarCorretagem(Model model) {
		List<NotaDeCorretagem> corretagens = corretagemRepository.findAllByOrderByDataAsc();
		corretagens.forEach(nc -> nc.setOrdens(ordemRepository.findByNotaDeCorretagem(nc)));
		model.addAttribute("corretagens", corretagens);
		return "basico/corretagem";
	}
	
	@PostMapping("/editar_corretagem")
	public String editarCorretagem(@RequestParam Map<String, String> parametros) {
		if(parametros.get("id") != null && parametros.get("editar_" + parametros.get("id")) != null ) {
			int id = Integer.parseInt(parametros.get("id"));
			double valorCorretagem = Double.parseDouble(parametros.get("valor_" + id));
			LocalDate data = LocalDate.parse(parametros.get("data_" + id));
			NotaDeCorretagem nc = new NotaDeCorretagem(data, valorCorretagem);
			nc.setId(id);
			corretagemRepository.save(nc);
		}
		return "redirect:/basico/corretagem";
	}
	
	@GetMapping("/deletar_corretagem")
	public String deletarCorretagem(@ModelAttribute(value="id") int id) {
		NotaDeCorretagem nc = corretagemRepository.getReferenceById(id);
		ordemRepository.findByNotaDeCorretagem(nc).forEach(ordem -> ordemRepository.deleteById(ordem.getId()));
		corretagemRepository.deleteById(id);
		return "redirect:/basico/corretagem";
	}
	
	@PostMapping("/corretagem")
	public String salvarNotaDeCorretagem(@RequestParam Map<String, String> parametros, Model model) {
		// Adicionar a nota de corretagem
		NotaDeCorretagem nc = corretagemRepository.save(new NotaDeCorretagem(LocalDate.parse(parametros.get("data")), 
				Double.parseDouble(parametros.get("valor_liquido"))));
		//Adicionar cada ordem setando o valor da nota de corretagem
		for(int i = 1; i <= Integer.parseInt(parametros.get("numero_de_ordens")); i++) {
			//System.out.println(i);
			String codigo = parametros.get("ticker"+i);
			Ticker ticker = tickerRepository.findByCodigo(codigo);
			if(ticker == null) {
				ticker = new Ticker(codigo);
				tickerRepository.save(ticker);
			}
			try {
				Ordem ordem = new Ordem(parametros.get("tipo"+i).charAt(0), Integer.parseInt(parametros.get("quantidade"+i)),
					ticker, Double.parseDouble(parametros.get("preco"+i)), nc);
				ordemRepository.save(ordem);
			} catch(Exception e) {
				model.addAttribute("mensagemDeErro", e.getMessage());
				return new ErroController().getErro(model);
			}
		}
		// Pegar a lista de corretagens do banco para para poder mostrar na view.
		// Lembrar de pegá-la ordenada.
		List<NotaDeCorretagem> corretagens = corretagemRepository.findAllByOrderByDataAsc();
		// Para cada nota de corretagem, a lista de ordens está vazia.
		// Por isso, é necessário setar a lista de ordens na nota de corretagem.
		for(NotaDeCorretagem notaDeCorretagem: corretagens)
			nc.setOrdens(ordemRepository.findByNotaDeCorretagem(notaDeCorretagem));
		model.addAttribute("corretagens", corretagens);
		// Por fim, redirecionar para a página onde as corretagens são mostradas.
		return "redirect:/basico/corretagem";
	}
}
