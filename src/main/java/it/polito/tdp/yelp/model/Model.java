package it.polito.tdp.yelp.model;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	private YelpDao dao;
	private Graph<Review, DefaultWeightedEdge> grafo;
	private List<Review> maggiori;
	private int valoreMaggiori;
	//ricorsione
	private List<Review> sequenza;
	private int giorni;
	
	
	
	public Model() {
		this.dao=new YelpDao();
	}

	public List<String> getAllCities() {
		List<String> citta=this.dao.getAllCities();
		
		return citta;
	}

	public List<Business> getAllBusiness(String citta) {
		return this.dao.getAllBusiness(citta);
	}

	public void creaGrafo(Business b) {
		this.grafo=new DefaultDirectedWeightedGraph<Review, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		List<Review> recensioni=this.dao.getAllReviews(b);
		Graphs.addAllVertices(this.grafo, recensioni);
		
		for(Review r: this.grafo.vertexSet()) {
			for(Review r1: this.grafo.vertexSet()) {
				if(r.getDate().isBefore(r1.getDate())) {
					double peso=r.getDate().until(r1.getDate(), ChronoUnit.DAYS);
					DefaultWeightedEdge e=this.grafo.addEdge(r, r1);
					this.grafo.setEdgeWeight(e, peso);
				}
			}
		}
		
		System.out.println(this.grafo.vertexSet().size());
		System.out.println(this.grafo.edgeSet().size());
	}

	public List<Review> getMaggiore() {
		this.maggiori=new ArrayList<>();
		this.valoreMaggiori=0;
		
		for(Review r: this.grafo.vertexSet()) {
			if(this.valoreMaggiori==0) {
				this.maggiori.add(r);
				this.valoreMaggiori=this.grafo.outgoingEdgesOf(r).size();
			}else if(this.valoreMaggiori<this.grafo.outgoingEdgesOf(r).size()){
				this.maggiori.clear();
				this.maggiori.add(r);
				this.valoreMaggiori=this.grafo.outgoingEdgesOf(r).size();
				
			}else if(this.valoreMaggiori==this.grafo.outgoingEdgesOf(r).size()) {
				this.maggiori.add(r);
				this.valoreMaggiori=this.grafo.outgoingEdgesOf(r).size();
			}
			
		}
		return this.maggiori;
		
	}

	public List<Review> getMaggiori() {
		return maggiori;
	}

	public int getValoreMaggiori() {
		return valoreMaggiori;
	}

	public List<Review> trovaSequenza() {
		this.sequenza=new ArrayList<>();
		List<Review> parziale =new ArrayList<>();
		this.giorni=0;
		
		
		for(Review r: this.grafo.vertexSet()) {
			parziale.add(r);
			cerca(parziale,this.grafo.outgoingEdgesOf(r),r,0);
			parziale.remove(parziale.size()-1);
		}
		return this.sequenza;
		
	}

	public int getGiorni() {
		return giorni;
	}

	private void cerca(List<Review> parziale, Set<DefaultWeightedEdge> outgoingEdges,Review last,int giorniP) {
		if(parziale.size()> this.sequenza.size()) {
			this.sequenza=new ArrayList<>(parziale);
			this.giorni=giorniP;
		}
		
		
		for(DefaultWeightedEdge e: outgoingEdges) {
			Review opposto=Graphs.getOppositeVertex(this.grafo, e, last);
			if(opposto.getStars()>=last.getStars() || !parziale.contains(opposto)) {
				parziale.add(opposto);
				cerca(parziale,this.grafo.outgoingEdgesOf(opposto),opposto,(int)(giorniP+this.grafo.getEdgeWeight(e)));
				parziale.remove(parziale.size()-1);
			}
		}
		
	}
	
	
	
	
	
}
