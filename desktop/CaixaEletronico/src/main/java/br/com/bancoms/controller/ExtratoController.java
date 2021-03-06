package br.com.bancoms.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Optional;

import br.com.bancoms.dto.MovimentoBuscaDTO;
import br.com.bancoms.model.Movimento;
import br.com.bancoms.service.ClienteService;
import br.com.bancoms.service.MovimentoService;
import br.com.bancoms.util.DateUtil;
import br.com.bancoms.view.ExtratoView;
import br.com.bancoms.view.MovimentoItemView;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class ExtratoController {

    private ClienteService clienteService = ClienteService.getInstance();
    private MovimentoService movimentoService = MovimentoService.getInstance();

    public ClienteController clienteController;
    private ExtratoView extratoView;

    public ExtratoController(ClienteController clienteController) {
        this.clienteController = clienteController;
        extratoView = new ExtratoView(this);
    }

    public void iniciarExtratos() {
        extratoView.iniciarView(clienteController);
    }


    public EventHandler<ActionEvent> cancelarAction() {
        return (event) -> clienteController.retornarMenuAction(extratoView);
    }

    public EventHandler<ActionEvent> detalhesMovimentoAction(MovimentoItemView itemView) {
        return (event) -> clienteService.consultarCliente(itemView.getMovimento().getNumeroContaDestino())
                .ifPresent(cliente -> {
                    extratoView.trocarDetalhes(itemView);
                    itemView.detalhesView(cliente, clienteController.clienteSessao);
                });
    }


    public EventHandler<ActionEvent> consultarExtratoAction() {        
        return (event) -> consultarExtratos(getMovimentoDTO())
                .ifPresent(lista -> {
                    extratoView.limparMovimentosView();
                    adicionarMovimentosView(lista);
                });
    }

    public double getSaldoDiferencial() {
        return movimentoService.getSaldoDiferencial(getMovimentoDTOSaldo());
    }

    public double getSaldoSemDiferencial() {
        double diferencialTotal = getSaldoDiferencial();
        return clienteController.getContaSessao().getSaldoSemDiferencial(diferencialTotal);
    }

    private void adicionarMovimentosView(ArrayList<Optional<Movimento>> lista) {
        lista.forEach(m -> addMovimentoItemView(m,lista.indexOf((m))));
    }

    public void addMovimentoItemView(Optional<Movimento> movimento, int indice){
        if(movimento.isPresent()){
            extratoView.adicionarMovimentoItem(movimento.get(), indice + 1);
        }
    }

    /* Retorna uma lista  de movimentos de acordo com opção selecionada na view*/
    private Optional<ArrayList<Optional<Movimento>>> consultarExtratos(MovimentoBuscaDTO busca) {

        if (extratoView.getTipoAtual().equals("Todos")) {
            return movimentoService.listarTodosMovimentos(busca);
        } else {
            busca.setTipoMovimento(extratoView.getTipoAtualValor());
            return movimentoService.listarMovimentosPorTipo(busca);
        }

    }


    private MovimentoBuscaDTO getMovimentoDTO() {

        MovimentoBuscaDTO dto = new MovimentoBuscaDTO();

        Calendar dateInicio = DateUtil.getCurrentCalendar();
        dateInicio.add(Calendar.DAY_OF_MONTH, -extratoView.getPeriodo());

        dto.setDataInicio(DateUtil.parseDefaultUSA(dateInicio));
        dto.setDataFim(DateUtil.getCurrentDateString());
        dto.setNumeroConta(clienteController.getContaSessao().getNumero());

        System.out.println(dto);

        return dto;
    }


    private MovimentoBuscaDTO getMovimentoDTOSaldo() {
        MovimentoBuscaDTO dto = new MovimentoBuscaDTO();

        Calendar dataInicio = DateUtil.getCurrentCalendar();
        dataInicio.set(Calendar.DAY_OF_MONTH, dataInicio.getActualMinimum(Calendar.DAY_OF_MONTH));
        dataInicio.set(Calendar.HOUR_OF_DAY, 0);
        dataInicio.set(Calendar.MINUTE, 0);
        dataInicio.set(Calendar.SECOND, 0);

        Calendar dataFim = DateUtil.getCurrentCalendar();
        dataFim.set(Calendar.DAY_OF_MONTH, dataFim.getActualMaximum(Calendar.DAY_OF_MONTH));
        dataFim.set(Calendar.HOUR_OF_DAY, 23);
        dataFim.set(Calendar.MINUTE, 59);
        dataFim.set(Calendar.SECOND, 59);

        dto.setDataInicio(DateUtil.parseDefaultUSA(dataInicio));
        dto.setDataFim(DateUtil.parseDefaultUSA(dataFim));
        dto.setNumeroConta(clienteController.getContaSessao().getNumero());

        return dto;
    }

}
