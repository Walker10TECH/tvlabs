package com.w3labs.tvlabs;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/**
 * LabsTV Backend
 * Implementação da API Spec "Rei dos Canais".
 * Endpoints:
 * - /channels (List, ID, Categories)
 * - /sports (List, ID, Categories)
 * - /search (Unified Search with Meta)
 */
@SpringBootApplication
@EnableScheduling
public class LabsTV {

    public static void main(String[] args) {
        SpringApplication.run(LabsTV.class, args);
    }

    // --- CONFIGURAÇÃO CORS ---
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 segundos para conectar
        factory.setReadTimeout(5000);    // 5 segundos para ler dados
        return new RestTemplate(factory);
    }

    // --- CARGA DE DADOS INICIAL (MOCK) ---
    @Bean
    @SuppressWarnings("unused")
    CommandLineRunner initData(ChannelRepository channelRepo, ScoreService scoreService) {
        return args -> {
            // 1. Carga de Canais (Lista Completa)
            List<Channel> channels = new ArrayList<>();
            
            // Helper para criar canais rapidamente
            String[][] rawData = {
                {"adultswim", "Adult Swim", "Infantil"},
                {"ae", "A&E", "Variedades"},
                {"globoam", "Globo AM", "TV Aberta"},
                {"animalplanet", "Animal Planet", "Documentários"},
                {"amc", "AMC", "Filmes"},
                {"aparecida", "TV Aparecida", "Variedades"},
                {"arte1", "Arte 1", "Cultura"},
                {"axn", "AXN", "Séries"},
                {"globoba", "Globo BA", "TV Aberta"},
                {"band", "Band", "TV Aberta"},
                {"bandnews", "BandNews", "Notícias"},
                {"bandsports", "BandSports", "Esportes"},
                {"bis", "Bis", "Música"},
                {"globodf", "Globo DF", "TV Aberta"},
                {"canalbrasil", "Canal Brasil", "Cultura"},
                {"canaloff", "Canal Off", "Esportes"},
                {"cartoonnetwork", "Cartoon Network", "Infantil"},
                {"cartoonito", "Cartoonito", "Infantil"},
                {"cazetv", "Cazé TV", "Esportes"},
                {"cazetv2", "Cazé TV 2", "Esportes"},
                {"cazetv3", "Cazé TV 3", "Esportes"},
                {"globoce", "Globo CE", "TV Aberta"},
                {"cinemax", "Cinemax", "Filmes"},
                {"cnnbrasil", "CNN Brasil", "Notícias"},
                {"combate", "Combate", "Esportes"},
                {"comedycentral", "Comedy Central", "Variedades"},
                {"curta", "Curta!", "Cultura"},
                {"discoverychannel", "Discovery Channel", "Documentários"},
                {"discoveryheh", "Discovery H&H", "Variedades"},
                {"discoveryid", "Discovery ID", "Documentários"},
                {"discoverykids", "Discovery Kids", "Infantil"},
                {"discoveryscience", "Discovery Science", "Documentários"},
                {"discoverytheater", "Discovery Theater", "Documentários"},
                {"discoveryturbo", "Discovery Turbo", "Documentários"},
                {"discoveryworld", "Discovery World", "Documentários"},
                {"e", "E! Entertainment", "Variedades"},
                {"globoes", "Globo ES", "TV Aberta"},
                {"espn", "ESPN", "Esportes"},
                {"espn2", "ESPN 2", "Esportes"},
                {"espn3", "ESPN 3", "Esportes"},
                {"espn4", "ESPN 4", "Esportes"},
                {"espn5", "ESPN 5", "Esportes"},
                {"espn6", "ESPN 6", "Esportes"},
                {"globonews", "GloboNews", "Notícias"},
                {"globoplaynovelas", "Globoplay Novelas", "Novelas"},
                {"gloob", "Gloob", "Infantil"},
                {"gloobinho", "Gloobinho", "Infantil"},
                {"gnt", "GNT", "Variedades"},
                {"globogo", "Globo GO", "TV Aberta"},
                {"hbo", "HBO", "Filmes"},
                {"hbo2", "HBO 2", "Filmes"},
                {"hbofamily", "HBO Family", "Filmes"},
                {"hboplus", "HBO Plus", "Filmes"},
                {"hbosignature", "HBO Signature", "Filmes"},
                {"hboxtreme", "HBO Xtreme", "Filmes"},
                {"hgtv", "HGTV", "Variedades"},
                {"history", "History", "Documentários"},
                {"history2", "History 2", "Documentários"},
                {"jpnews", "Jovem Pan News", "Notícias"},
                {"lifetime", "Lifetime", "Variedades"},
                {"globoma", "Globo MA", "TV Aberta"},
                {"max", "Max Prime", "Filmes"},
                {"max2", "Max", "Filmes"},
                {"max3", "Max 3", "Filmes"},
                {"megapix", "Megapix", "Filmes"},
                {"globomg", "Globo MG", "TV Aberta"},
                {"modoviagem", "Modo Viagem", "Variedades"},
                {"mtv", "MTV", "Música"},
                {"mtv00s", "MTV 00s", "Música"},
                {"mtvlive", "MTV Live", "Música"},
                {"multishow", "Multishow", "Variedades"},
                {"nbatv", "NBA TV", "Esportes"},
                {"nickjr", "Nick Jr", "Infantil"},
                {"nickelodeon", "Nickelodeon", "Infantil"},
                {"nsports", "NSports", "Esportes"},
                {"globopa", "Globo PA", "TV Aberta"},
                {"globopb", "Globo PB", "TV Aberta"},
                {"paramountnetwork", "Paramount Network", "Filmes"},
                {"paramountplus", "Paramount+", "Filmes"},
                {"globopr", "Globo PR", "TV Aberta"},
                {"globope", "Globo PE", "TV Aberta"},
                {"globopi", "Globo PI", "TV Aberta"},
                {"premiereclubes", "Premiere Clubes", "Esportes"},
                {"premiere2", "Premiere 2", "Esportes"},
                {"premiere3", "Premiere 3", "Esportes"},
                {"premiere4", "Premiere 4", "Esportes"},
                {"premiere5", "Premiere 5", "Esportes"},
                {"premiere6", "Premiere 6", "Esportes"},
                {"premiere7", "Premiere 7", "Esportes"},
                {"premiere8", "Premiere 8", "Esportes"},
                {"primevideo", "Prime Video", "Filmes"},
                {"record", "Record TV", "TV Aberta"},
                {"recordnews", "Record News", "Notícias"},
                {"redetv", "RedeTV!", "TV Aberta"},
                {"globorj", "Globo RJ", "TV Aberta"},
                {"globorn", "Globo RN", "TV Aberta"},
                {"globors", "Globo RS", "TV Aberta"},
                {"saborearte", "Sabor & Arte", "Variedades"},
                {"globosc", "Globo SC", "TV Aberta"},
                {"globosp", "Globo SP", "TV Aberta"},
                {"sbt", "SBT", "TV Aberta"},
                {"sonychannel", "Sony Channel", "Séries"},
                {"sonymovies", "Sony Movies", "Filmes"},
                {"space", "Space", "Filmes"},
                {"sportv", "SporTV", "Esportes"},
                {"sportv2", "SporTV 2", "Esportes"},
                {"sportv3", "SporTV 3", "Esportes"},
                {"sportv4", "SporTV 4", "Esportes"},
                {"studiouniversal", "Studio Universal", "Filmes"},
                {"telecineaction", "Telecine Action", "Filmes"},
                {"telecinecult", "Telecine Cult", "Filmes"},
                {"telecinefun", "Telecine Fun", "Filmes"},
                {"telecinepipoca", "Telecine Pipoca", "Filmes"},
                {"telecinepremium", "Telecine Premium", "Filmes"},
                {"telecinetouch", "Telecine Touch", "Filmes"},
                {"tlc", "TLC", "Variedades"},
                {"tnt", "TNT", "Filmes"},
                {"tntnovelas", "TNT Novelas", "Novelas"},
                {"tntseries", "TNT Séries", "Séries"},
                {"tooncast", "Tooncast", "Infantil"},
                {"tvcultura", "TV Cultura", "TV Aberta"},
                {"ufcfightpass", "UFC Fight Pass", "Esportes"},
                {"universaltv", "Universal TV", "Séries"},
                {"usa", "USA Network", "Séries"},
                {"warnertv", "Warner TV", "Séries"},
                {"woohoo", "Woohoo", "Esportes"},
                {"xsports", "XSports", "Esportes"}
            };

            for (String[] data : rawData) {
                String id = data[0];
                String name = data[1];
                String cat = data[2];
                String logo = "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&background=random&size=128&bold=true";
                String embed = "https://rdcanais.top/" + id;
                
                channels.add(new Channel(id, name, "Assista " + name + " ao vivo.", cat, logo, embed, true));
            }

            channelRepo.saveAll(channels);

            // 2. Evento Mock (Spec Request)
            SportEvent mockEvent = new SportEvent(
                "flamengo-vs-palmeiras", 
                "Flamengo vs Palmeiras", 
                "Final do Campeonato Brasileiro", 
                "Futebol", 
                "https://placehold.co/600x400/red/white?text=FLA+x+PAL", 
                LocalDateTime.of(2025, 8, 25, 20, 0), 
                LocalDateTime.of(2025, 8, 25, 22, 0), 
                "upcoming"
            );
            mockEvent.getEmbeds().add(new Embed("YouTube", "HD", "https://youtube.com/embed/abc123_mock"));
            mockEvent.getEmbeds().add(new Embed("Dailymotion", "FullHD", "https://dailymotion.com/embed/xyz456_mock"));
            
            // Salvar Mock + Sincronizar ESPN
            scoreService.saveEvent(mockEvent);
            scoreService.syncEvents();

            System.out.println("--- LabsTV API INICIADA (Porta 8080) ---");
        };
    }
}

// ================= ENTITIES (Modelos do Banco de Dados) =================

@Entity
@Table(name = "channels")
class Channel {
    @Id
    private String id;
    private String name;
    private String description;
    private String category;
    
    @JsonProperty("logo_url")
    private String logoUrl;
    
    @JsonProperty("embed_url")
    private String embedUrl;
    
    @JsonProperty("is_active")
    private boolean isActive;

    public Channel() {}
    public Channel(String id, String name, String description, String category, String logoUrl, String embedUrl, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.logoUrl = logoUrl;
        this.embedUrl = embedUrl;
        this.isActive = isActive;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getLogoUrl() { return logoUrl; }
    public String getEmbedUrl() { return embedUrl; }
    public boolean isActive() { return isActive; }

    // Compatibilidade com JSON solicitado
    public String getLogo() { return logoUrl; }
    public String getStreamUrl() { return embedUrl; }
}

@Entity
@Table(name = "sport_events")
class SportEvent {
    @Id
    private String id;
    private String title;
    private String description;
    private String category;
    private String poster;
    
    // Formatação de data específica para bater com o JSON da Spec: "2025-08-25 20:00:00"
    @JsonProperty("start_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonProperty("end_time")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    private String status; // live, upcoming, ended

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_embeds", joinColumns = @JoinColumn(name = "event_id"))
    private List<Embed> embeds = new ArrayList<>();

    public SportEvent() {}
    public SportEvent(String id, String title, String description, String category, String poster, LocalDateTime startTime, LocalDateTime endTime, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.poster = poster;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public String getPoster() { return poster; }
    public String getStatus() { return status; }
    public List<Embed> getEmbeds() { return embeds; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
}

@Embeddable
class Embed {
    private String provider;
    private String quality;
    
    @JsonProperty("embed_url")
    private String embedUrl;

    public Embed() {}
    public Embed(String provider, String quality, String embedUrl) {
        this.provider = provider;
        this.quality = quality;
        this.embedUrl = embedUrl;
    }

    public String getProvider() { return provider; }
    public String getQuality() { return quality; }
    public String getEmbedUrl() { return embedUrl; }
}

// ================= DTOs (Objetos de Resposta da API) =================

// Wrapper genérico { "success": true, "data": ... }
@JsonInclude(JsonInclude.Include.NON_NULL)
class ApiResponse<T> {
    public boolean success;
    public T data;
    public Integer total; // Usado em listas quando necessário
    public SearchMeta meta; // Usado na busca

    public ApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
    }
    
    public ApiResponse(boolean success, T data, int total) {
        this.success = success;
        this.data = data;
        this.total = total;
    }
    
    public ApiResponse(boolean success, T data, SearchMeta meta) {
        this.success = success;
        this.data = data;
        this.meta = meta;
    }
}

class CategoryDTO {
    public String id;
    public String name;
    public CategoryDTO(String name) {
        this.id = name.toLowerCase().replace(" ", "-");
        this.name = name;
    }
}

class SearchResultDTO {
    public List<Channel> channels;
    public List<SportEvent> events;

    public SearchResultDTO(List<Channel> channels, List<SportEvent> events) {
        this.channels = channels;
        this.events = events;
    }
}

class SearchMeta {
    public String query;
    @JsonProperty("total_channels")
    public int totalChannels;
    @JsonProperty("total_events")
    public int totalEvents;
    @JsonProperty("total_results")
    public int totalResults;

    public SearchMeta(String query, int totalChannels, int totalEvents) {
        this.query = query;
        this.totalChannels = totalChannels;
        this.totalEvents = totalEvents;
        this.totalResults = totalChannels + totalEvents;
    }
}

// ================= REPOSITÓRIOS (Acesso a Dados) =================

@Repository
interface ChannelRepository extends JpaRepository<Channel, String> {
    List<Channel> findByCategoryIgnoreCase(String category);
    
    @Query("SELECT DISTINCT c.category FROM Channel c")
    List<String> findDistinctCategories();

    @Query("SELECT c FROM Channel c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Channel> search(String query);
}

@Repository
interface SportEventRepository extends JpaRepository<SportEvent, String> {
    List<SportEvent> findByCategoryIgnoreCase(String category);
    List<SportEvent> findByStatusIgnoreCase(String status);
    List<SportEvent> findByCategoryIgnoreCaseAndStatusIgnoreCase(String category, String status);

    @Query("SELECT DISTINCT e.category FROM SportEvent e")
    List<String> findDistinctCategories();

    @Query("SELECT e FROM SportEvent e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<SportEvent> search(String query);
}

// ================= SERVICES =================

@Service
class ScoreService {
    private final RestTemplate restTemplate;
    private final SportEventRepository eventRepo;

    public ScoreService(RestTemplate restTemplate, SportEventRepository eventRepo) {
        this.restTemplate = restTemplate;
        this.eventRepo = eventRepo;
    }

    public void saveEvent(SportEvent event) {
        eventRepo.save(event);
    }

    @Scheduled(fixedRate = 300000) // Sincroniza a cada 5 minutos
    public void syncEvents() {
        syncCategory("soccer", "bra.1", "Futebol");
        syncCategory("basketball", "nba", "Basquete");
    }

    @SuppressWarnings({ "unchecked", "UseSpecificCatch" })
    private void syncCategory(String sport, String league, String categoryName) {
        String url = String.format("http://site.api.espn.com/apis/site/v2/sports/%s/%s/scoreboard", sport, league);
        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("events")) {
                List<Map<String, Object>> events = (List<Map<String, Object>>) response.get("events");
                for (Map<String, Object> eventData : events) {
                    try {
                        SportEvent event = mapToSportEvent(eventData, categoryName);
                        eventRepo.save(event);
                    } catch (Exception e) {
                        System.err.println("Erro ao processar evento: " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao sincronizar " + categoryName + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private SportEvent mapToSportEvent(Map<String, Object> data, String category) {
        String id = (String) data.get("id");
        String title = (String) data.get("name");
        String shortName = (String) data.get("shortName");
        
        String dateStr = (String) data.get("date");
        ZonedDateTime zdt = ZonedDateTime.parse(dateStr);
        LocalDateTime start = zdt.toLocalDateTime();
        LocalDateTime end = start.plusHours(2);

        Map<String, Object> statusObj = (Map<String, Object>) data.get("status");
        Map<String, Object> typeObj = (Map<String, Object>) statusObj.get("type");
        String state = (String) typeObj.get("state");
        String status = "upcoming";
        if ("in".equalsIgnoreCase(state)) status = "live";
        else if ("post".equalsIgnoreCase(state)) status = "ended";

        String poster = "https://placehold.co/600x400?text=" + category;
        try {
            List<Map<String, Object>> competitions = (List<Map<String, Object>>) data.get("competitions");
            if (!competitions.isEmpty()) {
                List<Map<String, Object>> competitors = (List<Map<String, Object>>) competitions.get(0).get("competitors");
                if (!competitors.isEmpty()) {
                    Map<String, Object> team = (Map<String, Object>) competitors.get(0).get("team");
                    if (team.containsKey("logo")) poster = (String) team.get("logo");
                }
            }
        } catch (Exception ignored) {}

        return new SportEvent(id, title, shortName, category, poster, start, end, status);
    }

    @SuppressWarnings({ "unchecked", "UseSpecificCatch" })
    public Map<String, Object> getLiveScores(String category) {
        String sport = "soccer";
        String league = "bra.1"; // Default Brasileirão

        if (category != null) {
            if (category.equalsIgnoreCase("basquete") || category.equalsIgnoreCase("basketball")) {
                sport = "basketball";
                league = "nba";
            } else if (category.equalsIgnoreCase("futebol") || category.equalsIgnoreCase("soccer")) {
                sport = "soccer";
                league = "bra.1";
            } else if (category.equalsIgnoreCase("nfl") || category.equalsIgnoreCase("futebol americano")) {
                sport = "football";
                league = "nfl";
            }
        }

        String url = String.format("http://site.api.espn.com/apis/site/v2/sports/%s/%s/scoreboard", sport, league);
        
        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            return Map.of("error", "Failed to fetch scores: " + e.getMessage());
        }
    }
}

// ================= CONTROLLER (Endpoints REST) =================

@RestController
@SuppressWarnings("unused")
class ApiController {

    private final ChannelRepository channelRepo;
    private final SportEventRepository eventRepo;
    private final ScoreService scoreService;
    private final RestTemplate restTemplate;

    public ApiController(ChannelRepository channelRepo, SportEventRepository eventRepo, ScoreService scoreService, RestTemplate restTemplate) {
        this.channelRepo = channelRepo;
        this.eventRepo = eventRepo;
        this.scoreService = scoreService;
        this.restTemplate = restTemplate;
    }

    // Redireciona a raiz para o frontend
    @GetMapping("/")
    public ResponseEntity<Void> index() {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/TVLabs.html")).build();
    }

    // --- ENDPOINTS DE CANAIS ---

    @GetMapping("/channels")
    public ResponseEntity<ApiResponse<List<Channel>>> getChannels(@RequestParam(required = false) String category) {
        List<Channel> channels;
        if (category != null && !category.isEmpty()) {
            channels = channelRepo.findByCategoryIgnoreCase(category);
        } else {
            channels = channelRepo.findAll();
        }
        return ResponseEntity.ok(new ApiResponse<>(true, channels, channels.size()));
    }

    @GetMapping("/channels/{id}")
    public ResponseEntity<?> getChannelById(@PathVariable String id) {
        return channelRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/channels/categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getChannelCategories() {
        List<String> rawCategories = channelRepo.findDistinctCategories();
        List<CategoryDTO> categories = rawCategories.stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
        // Spec exige wrapper com total
        return ResponseEntity.ok(new ApiResponse<>(true, categories, categories.size()));
    }

    // --- ENDPOINTS DE ESPORTES ---

    @GetMapping("/sports")
    public ResponseEntity<?> getSports(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        
        List<SportEvent> events;
        if (category != null && status != null) {
            events = eventRepo.findByCategoryIgnoreCaseAndStatusIgnoreCase(category, status);
        } else if (category != null) {
            events = eventRepo.findByCategoryIgnoreCase(category);
        } else if (status != null) {
            events = eventRepo.findByStatusIgnoreCase(status);
        } else {
            events = eventRepo.findAll();
        }
        return ResponseEntity.ok(new ApiResponse<>(true, events, events.size()));
    }

    @GetMapping("/sports/{id}")
    public ResponseEntity<?> getSportById(@PathVariable String id) {
        return eventRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sports/categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getSportCategories() {
        List<String> rawCategories = eventRepo.findDistinctCategories();
        List<CategoryDTO> categories = rawCategories.stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse<>(true, categories, categories.size()));
    }

    // --- SCORES ENDPOINT (ESPN INTEGRATION) ---

    @GetMapping("/scores")
    public ResponseEntity<?> getScores(@RequestParam(required = false, defaultValue = "futebol") String category) {
        return ResponseEntity.ok(scoreService.getLiveScores(category));
    }

    // --- ENDPOINT DE BUSCA UNIFICADA ---

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchResultDTO>> search(@RequestParam String q) {
        List<Channel> channels = channelRepo.search(q);
        List<SportEvent> events = eventRepo.search(q);

        SearchResultDTO result = new SearchResultDTO(channels, events);
        SearchMeta meta = new SearchMeta(q, channels.size(), events.size());

        // Spec exige wrapper com meta
        return ResponseEntity.ok(new ApiResponse<>(true, result, meta));
    }

    // --- PROXY INTERNO (Evita CORS no Frontend) ---
    @GetMapping("/proxy")
    public ResponseEntity<String> proxy(@RequestParam String url) {
        try {
            String content = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok().header("Content-Type", "application/json").body(content);
        } catch (RestClientException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}

// ================= CONFIG CONTROLLER (Frontend Data) =================

@RestController
@RequestMapping("/api/config")
@SuppressWarnings({"unused"})
class ConfigController {

    @GetMapping("/sports")
    public ResponseEntity<String> getSportsConfig() {
        return ResponseEntity.ok(SportsData.JSON);
    }

    @GetMapping("/wiki")
    public ResponseEntity<String> getWikiConfig() {
        return ResponseEntity.ok(WikiData.JSON);
    }
}

class SportsData {
    public static final String JSON = """
    {
        "soccer": {
            "name": "Futebol",
            "leagues": {
                "brasileirao": { "id": 71, "slug": "bra.1", "name": "Brasileirão Série A", "logo": "https://media.api-sports.io/football/leagues/71.png", "bg": "https://admin.cnnbrasil.com.br/wp-content/uploads/sites/12/2024/04/taca-e1712177532245.jpeg?w=910", "wikiPage": "Campeonato_Brasileiro_de_Futebol_de_{year}_-_Série_A" },
                "brasileiraob": { "id": 72, "slug": "bra.2", "name": "Brasileirão Série B", "logo": "https://media.api-sports.io/football/leagues/72.png", "bg": "https://tse2.mm.bing.net/th/id/OIP.r04uwPhHqix_1NMlertKLAHaEK?rs=1&pid=ImgDetMain&o=7&rm=3", "wikiPage": "Campeonato_Brasileiro_de_Futebol_de_{year}_-_Série_B" },
                "libertadores": { "id": 13, "slug": "conmebol.libertadores", "name": "Libertadores", "logo": "https://media.api-sports.io/football/leagues/13.png", "bg": "https://lncimg.lance.com.br/cdn-cgi/image/width=950,quality=75,fit=pad,format=webp/uploads/2021/01/29/60141dfea45a6.jpeg", "wikiPage": "Copa_Libertadores_da_América_de_{year}" },
                "sulamericana": { "id": 11, "slug": "conmebol.sudamericana", "name": "Sul-Americana", "logo": "https://media.api-sports.io/football/leagues/11.png", "bg": "https://s2-ge.glbimg.com/-M2a9kZoesn0lYulcHlg0hIvCko=/0x0:1280x853/984x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_bc8228b6673f488aa253bbcb03c80ec5/internal_photos/bs/2022/j/Y/BAJcXyRz6Iz6Svtlf1AA/244ecb3d-2cab-4933-87e0-de24261e1696.jfif", "wikiPage": "Copa_Sul-Americana_de_{year}" },
                "champions": { "id": 2, "slug": "uefa.champions", "name": "Champions League", "logo": "https://media.api-sports.io/football/leagues/2.png", "bg": "https://lncimg.lance.com.br/cdn-cgi/image/width=1280,height=720,quality=75,fit=cover/uploads/2023/03/17/641465f53b411.jpeg", "wikiPage": "Liga_dos_Campeões_da_UEFA_de_{year}-{next_year}" },
                "mundial-de-clubes": { "id": 15, "slug": "fifa.club.world.cup", "name": "Mundial de Clubes", "logo": "https://media.api-sports.io/football/leagues/15.png", "bg": "https://lncimg.lance.com.br/cdn-cgi/image/width=950,quality=75,fit=pad,format=webp/uploads/2020/04/05/5e8a04ba97834.jpeg", "wikiPage": "Premier_League_de_{year}-{next_year}" },
                "premier": { "id": 39, "slug": "eng.1", "name": "Premier League", "logo": "https://media.api-sports.io/football/leagues/39.png", "bg": "https://t3.ftcdn.net/jpg/02/73/68/18/360_F_273681812_uA2F2l1533aT2r1o5c3sJz5sJgqG2s4u.jpg", "wikiPage": "Premier_League_de_{year}-{next_year}" },
                "laliga": { "id": 140, "slug": "esp.1", "name": "La Liga", "logo": "https://media.api-sports.io/football/leagues/140.png", "bg": "https://assets.goal.com/images/v3/blt054a5ddddf1e5a2b/158f203189e94419d7010667f379da35bcf16d8e.jpg", "wikiPage": "La_Liga_de_{year}-{next_year}" },
                "seriea": { "id": 135, "slug": "ita.1", "name": "Serie A", "logo": "https://media.api-sports.io/football/leagues/135.png", "bg": "https://cloudfront-us-east-1.images.arcpublishing.com/newr7/L6ZP3CEJ6VMPNFT5HTW7H7L7LY.jpg", "wikiPage": "Campeonato_Italiano_de_Futebol_de_{year}-{next_year}_-_Série_A" },
                "bundesliga": { "id": 78, "slug": "ger.1", "name": "Bundesliga", "logo": "https://media.api-sports.io/football/leagues/78.png", "bg": "https://s2-ge.glbimg.com/F2PP74GbwM16ougDWVMDhZzEp6U=/0x0:1024x659/984x0/smart/filters:strip_icc()/i.s3.glbimg.com/v1/AUTH_bc8228b6673f488aa253bbcb03c80ec5/internal_photos/bs/2024/X/Y/3pfuBhTzuraB6EHOqszA/gettyimages-1742744089.jpg", "wikiPage": "Bundesliga_de_{year}-{next_year}" },
                "saudi": { "id": 307, "slug": "ksa.1", "name": "Saudita", "logo": "https://media.api-sports.io/football/leagues/307.png", "bg": "https://www.365scores.com/pt-br/news/magazine/wp-content/uploads/2023/11/366423961_5646928382077000_2604818796297545939_n-e1699379331310.jpg", "wikiPage": "Liga_Profissional_Saudita_de_{year}-{next_year}" },
                "eredivisie": { "id": 88, "slug": "ned.1", "name": "Eredivisie", "logo": "https://media.api-sports.io/football/leagues/88.png", "bg": "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcSFGutuFAjY1Jn4Egu_ncE2-IMj_zUDooPWDQ&s", "wikiPage": "Eredivisie_de_{year}-{next_year}" },
                "argentina": { "id": 128, "slug": "arg.1", "name": "Argentina", "logo": "https://media.api-sports.io/football/leagues/128.png", "bg": "https://admin.cnnbrasil.com.br/wp-content/uploads/sites/12/2024/12/Capturar_e27e29.jpg?w=793", "wikiPage": "Campeonato_Argentino_de_Futebol_de_{year}" }
            }
        },
        "basketball": { 
            "name": "Basquete", 
            "leagues": { 
                "nba": { "id": "nba", "slug": "nba", "name": "NBA", "logo": "https://a.espncdn.com/i/teamlogos/leagues/500/nba.png", "bg": "https://wallpapercave.com/wp/wp8862445.jpg", "wikiPage": "National_Basketball_Association" },
                "wnba": { "id": "wnba", "slug": "wnba", "name": "WNBA", "logo": "https://a.espncdn.com/i/teamlogos/leagues/500/wnba.png", "bg": "https://wallpapercave.com/wp/wp9103473.jpg", "wikiPage": "Women%27s_National_Basketball_Association" },
                "ncaam": { "id": "ncaam", "slug": "mens-college-basketball", "name": "NCAA Masculino", "logo": "https://a.espncdn.com/i/teamlogos/leagues/500/mens-college-basketball.png", "bg": "https://wallpapercave.com/wp/wp1841421.jpg", "wikiPage": "NCAA_Division_I_men%27s_basketball_tournament" },
                "ncaaw": { "id": "ncaaw", "slug": "womens-college-basketball", "name": "NCAA Feminino", "logo": "https://a.espncdn.com/i/teamlogos/leagues/500/womens-college-basketball.png", "bg": "https://cdn.theathletic.com/app/uploads/2023/04/03005856/GettyImages-1250411347-scaled.jpg", "wikiPage": "NCAA_Division_I_women%27s_basketball_tournament" }
            } 
        },
        "football": { 
            "name": "Fut. Americano", 
            "leagues": { 
                "nfl": { "id": "nfl", "slug": "nfl", "name": "NFL", "logo": "https://a.espncdn.com/i/teamlogos/leagues/500/nfl.png", "bg": "https://wallpapercave.com/wp/wp7511105.jpg", "wikiPage": "National_Football_League" },
                "college-football": { "id": "college-football", "slug": "college-football", "name": "College Football", "logo": "https://a.espncdn.com/i/teamlogos/leagues/500/college-football.png", "bg": "https://wallpapercave.com/wp/wp2367688.jpg", "wikiPage": "College_football" }
            } 
        },
        "baseball": {
            "name": "Baseball",
            "leagues": {
                "mlb": { "id": "mlb", "slug": "mlb", "name": "MLB", "logo": "https://a.espncdn.com/i/teamlogos/leagues/500/mlb.png", "bg": "https://wallpapercave.com/wp/wp2763759.jpg", "wikiPage": "Major_League_Baseball" },
                "college-baseball": { "id": "college-baseball", "slug": "college-baseball", "name": "College Baseball", "logo": "https://a.espncdn.com/i/teamlogos/leagues/500/college-baseball.png", "bg": "https://www.ncaa.com/_flysystem/public-s3/styles/original/public-s3/images/2021-06-30/CWS-2021-trophy.jpg", "wikiPage": "College_World_Series" }
            }
        },
        "hockey": {
            "name": "Hóquei",
            "leagues": {
                "nhl": { "id": "nhl", "slug": "nhl", "name": "NHL", "logo": "https://a.espncdn.com/i/teamlogos/leagues/500/nhl.png", "bg": "https://wallpapercave.com/wp/wp4911526.jpg", "wikiPage": "National_Hockey_League" }
            }
        }
    }
    """;
}

class WikiData {
    public static final String JSON = """
    {
        "brasileirao": { 
            "Athletico Paranaense": "Club_Athletico_Paranaense",
            "Atlético Goianiense": "Atlético_Clube_Goianiense",
            "Atlético Mineiro": "Clube_Atlético_Mineiro",
            "Bahia": "Esporte_Clube_Bahia",
            "Botafogo": "Botafogo_de_Futebol_e_Regatas",
            "Corinthians": "Sport_Club_Corinthians_Paulista",
            "Criciúma": "Criciúma_Esporte_Clube",
            "Cruzeiro": "Cruzeiro_Esporte_Clube",
            "Cuiabá": "Cuiabá_Esporte_Clube",
            "Flamengo": "Clube_de_Regatas_do_Flamengo",
            "Fluminense": "Fluminense_Football_Club",
            "Fortaleza": "Fortaleza_Esporte_Clube",
            "Grêmio": "Grêmio_Foot-Ball_Porto_Alegrense",
            "Internacional": "Sport_Club_Internacional",
            "Juventude": "Esporte_Clube_Juventude",
            "Palmeiras": "Sociedade_Esportiva_Palmeiras",
            "Red Bull Bragantino": "Red_Bull_Bragantino",
            "São Paulo": "São_Paulo_Futebol_Clube",
            "Vasco da Gama": "Club_de_Regatas_Vasco_da_Gama",
            "Vitória": "Esporte_Clube_Vitória",
            "Amazonas": "Amazonas_Futebol_Clube",
            "América-MG": "América_Futebol_Clube_(Minas_Gerais)",
            "Avaí": "Avaí_Futebol_Clube",
            "Botafogo-SP": "Botafogo_Futebol_Clube_(Ribeirão_Preto)",
            "Brusque": "Brusque_Futebol_Clube",
            "Ceará": "Ceará_Sporting_Club",
            "Chapecoense": "Associação_Chapecoense_de_Futebol",
            "Coritiba": "Coritiba_Foot_Ball_Club",
            "CRB": "Clube_de_Regatas_Brasil",
            "Goiás": "Goiás_Esporte_Clube",
            "Guarani": "Guarani_Futebol_Clube",
            "Ituano": "Ituano_Futebol_Clube",
            "Mirassol": "Mirassol_Futebol_Clube",
            "Novorizontino": "Grêmio_Novorizontino",
            "Operário-PR": "Operário_Ferroviário_Esporte_Clube",
            "Paysandu": "Paysandu_Sport_Club",
            "Ponte Preta": "Associação_Atlética_Ponte_Preta",
            "Santos": "Santos_Futebol_Clube",
            "Sport": "Sport_Club_do_Recife",
            "Vila Nova": "Vila_Nova_Futebol_Clube"
        },
        "brasileiraob": {
            "Amazonas": "Amazonas_Futebol_Clube",
            "América-MG": "América_Futebol_Clube_(Minas_Gerais)",
            "Avaí": "Avaí_Futebol_Clube",
            "Botafogo-SP": "Botafogo_Futebol_Clube_(Ribeirão_Preto)",
            "Brusque": "Brusque_Futebol_Clube",
            "Ceará": "Ceará_Sporting_Club",
            "Chapecoense": "Associação_Chapecoense_de_Futebol",
            "Coritiba": "Coritiba_Foot_Ball_Club",
            "CRB": "Clube_de_Regatas_Brasil",
            "Goiás": "Goiás_Esporte_Clube",
            "Guarani": "Guarani_Futebol_Clube",
            "Ituano": "Ituano_Futebol_Clube",
            "Mirassol": "Mirassol_Futebol_Clube",
            "Novorizontino": "Grêmio_Novorizontino",
            "Operário-PR": "Operário_Ferroviário_Esporte_Clube",
            "Paysandu": "Paysandu_Sport_Club",
            "Ponte Preta": "Associação_Atlética_Ponte_Preta",
            "Santos": "Santos_Futebol_Clube",
            "Sport": "Sport_Club_do_Recife",
            "Vila Nova": "Vila_Nova_Futebol_Clube"
        },
        "premier": {
            "Manchester City": "Manchester_City_F.C.",
            "Liverpool": "Liverpool_F.C.",
            "Arsenal": "Arsenal_F.C.",
            "Chelsea": "Chelsea_F.C.",
            "Tottenham Hotspur": "Tottenham_Hotspur_F.C.",
            "Newcastle United": "Newcastle_United_F.C.",
            "Manchester United": "Manchester_United_F.C.",
            "Aston Villa": "Aston_Villa_F.C.",
            "Everton": "Everton_F.C.",
            "West Ham United": "West_Ham_United_F.C.",
            "Leicester City": "Leicester_City_F.C.",
            "Wolverhampton Wanderers": "Wolverhampton_Wanderers_F.C.",
            "Southampton": "Southampton_F.C.",
            "Ipswich Town": "Ipswich_Town_F.C.",
            "Nottingham Forest": "Nottingham_Forest_F.C.",
            "Leeds United": "Leeds_United_F.C.",
            "Fulham": "Fulham_F.C.",
            "Crystal Palace": "Crystal_Palace_F.C.",
            "Brighton & Hove Albion": "Brighton_&_Hove_Albion_F.C.",
            "Bournemouth": "A.F.C._Bournemouth",
            "Brentford": "Brentford_F.C.",
            "Burnley": "Burnley_F.C.",
            "Luton Town": "Luton_Town_F.C.",
            "Sheffield United": "Sheffield_United_F.C.",
            "West Bromwich Albion": "West_Bromwich_Albion_F.C.",
            "Hull City": "Hull_City_A.F.C.",
            "Coventry City": "Coventry_City_F.C.",
            "Middlesbrough": "Middlesbrough_F.C.",
            "Norwich City": "Norwich_City_F.C.",
            "Bristol City": "Bristol_City_F.C.",
            "Cardiff City": "Cardiff_City_F.C.",
            "Swansea City": "Swansea_City_A.F.C.",
            "Watford": "Watford_F.C.",
            "Sunderland": "Sunderland_A.F.C.",
            "Stoke City": "Stoke_City_F.C.",
            "Queens Park Rangers": "Queens_Park_Rangers_F.C.",
            "Blackburn Rovers": "Blackburn_Rovers_F.C.",
            "Sheffield Wednesday": "Sheffield_Wednesday_F.C.",
            "Derby County": "Derby_County_F.C.",
            "Portsmouth": "Portsmouth_F.C."
        },
        "laliga": {
            "Real Madrid": "Real_Madrid_Club_de_Fútbol",
            "Barcelona": "Fútbol_Club_Barcelona",
            "Atlético Madrid": "Club_Atlético_de_Madrid",
            "Sevilla": "Sevilla_Fútbol_Club",
            "Valencia": "Valencia_Club_de_Fútbol",
            "Real Sociedad": "Real_Sociedad_de_Fútbol",
            "Athletic Bilbao": "Athletic_Club",
            "Getafe": "Getafe_Club_de_Fútbol",
            "Villarreal": "Villarreal_Club_de_Fútbol",
            "Real Betis": "Real_Betis_Club_de_Fútbol",
            "Celta Vigo": "Real_Club_Celta_de_Vigo",
            "Osasuna": "Club_Atlético_Osasuna",
            "Alavés": "Deportivo_Alavés",
            "Levante": "Levante_Unión_Deportiva",
            "Elche": "Elche_Club_de_Fútbol",
            "Cádiz": "Cádiz_Club_de_Fútbol",
            "Mallorca": "Real_Club_Deportivo_Mallorca",
            "Rayo Vallecano": "Rayo_Vallecano",
            "Espanyol": "RCD_Espanyol",
            "Valladolid": "Real_Valladolid",
            "Granada": "Granada_Club_de_Fútbol",
            "Almería": "Unión_Deportiva_Almería",
            "Girona": "Girona_Fútbol_Club",
            "Las Palmas": "Unión_Deportiva_Las_Palmas",
            "Leganés": "Club_Deportivo_Leganés",
            "Huesca": "Sociedad_Deportiva_Huesca",
            "Eibar": "Sociedad_Deportiva_Eibar",
            "Real Zaragoza": "Real_Zaragoza",
            "Sporting Gijón": "Real_Sporting_de_Gijón",
            "Oviedo": "Real_Oviedo",
            "Deportivo La Coruña": "Real_Club_Deportivo_de_La_Coruña",
            "Málaga": "Málaga_Club_de_Fútbol",
            "Tenerife": "Club_Deportivo_Tenerife",
            "Córdoba": "Córdoba_Club_de_Fútbol",
            "Recreativo": "Real_Club_Recreativo_de_Huelva",
            "Numancia": "Club_Deportivo_Numancia_de_Soria",
            "Racing Santander": "Real_Racing_Club_de_Santander",
            "Albacete": "Albacete_Balompié",
            "Burgos": "Burgos_Club_de_Fútbol",
            "FC Cartagena": "Fútbol_Club_Cartagena",
            "Mirandés": "Club_Deportivo_Mirandés"
        },
        "seriea": {
            "Juventus": "Juventus_Football_Club",
            "Inter": "Football_Club_Internazionale_Milano",
            "Milan": "Associazione_Calcio_Milan",
            "Napoli": "Società_Sportiva_Calcio_Napoli",
            "AS Roma": "Associazione_Sportiva_Roma",
            "Roma": "Associazione_Sportiva_Roma",
            "Lazio": "Società_Sportiva_Lazio",
            "Fiorentina": "ACF_Fiorentina",
            "Atalanta": "Atalanta_Bergamasca_Calcio",
            "Bologna": "Bologna_F.C._1909",
            "Torino": "Torino_F.C.",
            "Genoa": "Genoa_C.F.C.",
            "Monza": "A.C._Monza",
            "Lecce": "U.S._Lecce",
            "Udinese": "Udinese_Calcio",
            "Cagliari": "Cagliari_Calcio",
            "Empoli": "Empoli_F.C.",
            "Hellas Verona": "Hellas_Verona_F.C.",
            "Parma": "Parma_Calcio_1913",
            "Como": "Como_1907",
            "Venezia": "Venezia_F.C.",
            "Cremonese": "U.S._Cremonese",
            "Catanzaro": "U.S._Catanzaro_1929",
            "Palermo": "Palermo_F.C.",
            "Sampdoria": "U.C._Sampdoria",
            "Brescia": "Brescia_Calcio",
            "Pisa": "Pisa_S.C.",
            "Reggiana": "A.C._Reggiana_1919",
            "Modena": "Modena_F.C.",
            "Südtirol": "F.C._Südtirol",
            "Cosenza": "Cosenza_Calcio",
            "Spezia": "Spezia_Calcio",
            "Ternana": "Ternana_Calcio",
            "Bari": "S.S.C._Bari",
            "Ascoli": "Ascoli_Calcio_1898_F.C.",
            "Frosinone": "Frosinone_Calcio",
            "Salernitana": "U.S._Salernitana_1919",
            "Sassuolo": "U.S._Sassuolo_Calcio"
        },
        "bundesliga": {
            "Bayern Munich": "Fußball-Club_Bayern_München",
            "Borussia Dortmund": "Ballspielverein_Borussia_09_e._V._Dortmund",
            "RB Leipzig": "RB_Leipzig",
            "Bayer Leverkusen": "Bayer_04_Leverkusen",
            "VfB Stuttgart": "VfB_Stuttgart",
            "Eintracht Frankfurt": "Eintracht_Frankfurt",
            "FC St. Pauli": "FC_St._Pauli",
            "Holstein Kiel": "Holstein_Kiel",
            "Fortuna Düsseldorf": "Fortuna_Düsseldorf",
            "Hamburger SV": "Hamburger_SV",
            "Karlsruher SC": "Karlsruher_SC",
            "Hannover 96": "Hannover_96",
            "SC Paderborn 07": "SC_Paderborn_07",
            "Greuther Fürth": "SpVgg_Greuther_Fürth",
            "Hertha BSC": "Hertha_BSC",
            "Schalke 04": "FC_Schalke_04",
            "1. FC Nürnberg": "1._FC_Nürnberg",
            "1. FC Kaiserslautern": "1._FC_Kaiserslautern",
            "1. FC Magdeburg": "1._FC_Magdeburg",
            "Eintracht Braunschweig": "Eintracht_Braunschweig",
            "VfL Osnabrück": "VfL_Osnabrück",
            "Wehen Wiesbaden": "SV_Wehen_Wiesbaden",
            "Hansa Rostock": "F.C._Hansa_Rostock",
            "SSV Ulm 1846": "SSV_Ulm_1846",
            "Preussen Münster": "SC_Preußen_Münster",
            "Jahn Regensburg": "SSV_Jahn_Regensburg",
            "Dynamo Dresden": "Dynamo_Dresden",
            "1. FC Saarbrücken": "1._FC_Saarbrücken",
            "Erzgebirge Aue": "FC_Erzgebirge_Aue",
            "Rot-Weiss Essen": "Rot-Weiss_Essen",
            "SV Sandhausen": "SV_Sandhausen",
            "SpVgg Unterhaching": "SpVgg_Unterhaching",
            "FC Ingolstadt 04": "FC_Ingolstadt_04",
            "Borussia Mönchengladbach": "Borussia_Mönchengladbach",
            "VfL Wolfsburg": "VfL_Wolfsburg",
            "TSG Hoffenheim": "TSG_1899_Hoffenheim",
            "SC Freiburg": "SC_Freiburg",
            "FC Augsburg": "FC_Augsburg",
            "Werder Bremen": "SV_Werder_Bremen",
            "Mainz 05": "1._FSV_Mainz_05",
            "1. FC Köln": "1._FC_Köln",
            "VfL Bochum": "VfL_Bochum",
            "Union Berlin": "1._FC_Union_Berlin",
            "Darmstadt 98": "SV_Darmstadt_98"
        },
        "libertadores": {
            "Flamengo": "Clube_de_Regatas_do_Flamengo",
            "Palmeiras": "Sociedade_Esportiva_Palmeiras",
            "Corinthians": "Sport_Club_Corinthians_Paulista",
            "São Paulo": "São_Paulo_Futebol_Clube",
            "Grêmio": "Grêmio_Foot-Ball_Porto_Alegrense",
            "Internacional": "Sport_Club_Internacional",
            "Atlético Mineiro": "Clube_Atlético_Mineiro",
            "Fluminense": "Fluminense_Football_Club",
            "Botafogo": "Botafogo_de_Futebol_e_Regatas",
            "Vasco da Gama": "Club_de_Regatas_Vasco_da_Gama",
            "River Plate": "Club_Atlético_River_Plate",
            "Boca Juniors": "Club_Atlético_Boca_Juniors",
            "Independiente": "Club_Atlético_Independiente",
            "Racing Club": "Racing_Club_de_Avellaneda",
            "San Lorenzo": "Club_Atlético_San_Lorenzo_de_Almagro",
            "Estudiantes": "Estudiantes_de_La_Plata",
            "Vélez Sarsfield": "Club_Atlético_Vélez_Sarsfield",
            "Rosario Central": "Club_Atlético_Rosario_Central",
            "Talleres": "Club_Atlético_Talleres",
            "Peñarol": "Club_Atlético_Peñarol",
            "Nacional": "Club_Nacional_de_Football",
            "Colo-Colo": "Colo-Colo",
            "Universidad de Chile": "Club_Universidad_de_Chile",
            "Universidad Católica": "Club_Deportivo_Universidad_Católica",
            "Olimpia": "Club_Olimpia",
            "Cerro Porteño": "Club_Cerro_Porteño",
            "Libertad": "Club_Libertad",
            "Atlético Nacional": "Atlético_Nacional",
            "Millonarios": "Millonarios_Fútbol_Club",
            "Junior": "Junior_de_Barranquilla",
            "LDU Quito": "LDU_Quito",
            "Barcelona SC": "Barcelona_Sporting_Club",
            "Independiente del Valle": "Independiente_del_Valle",
            "Bolívar": "Club_Bolívar",
            "The Strongest": "The_Strongest",
            "Alianza Lima": "Club_Alianza_Lima",
            "Universitario": "Club_Universitario_de_Deportes",
            "Sporting Cristal": "Club_Sporting_Cristal",
            "Caracas FC": "Caracas_Fútbol_Club",
            "Deportivo Táchira": "Deportivo_Táchira_Fútbol_Club"
        },
        "sulamericana": {
            "Corinthians": "Sport_Club_Corinthians_Paulista",
            "Internacional": "Sport_Club_Internacional",
            "Atlético Mineiro": "Clube_Atlético_Mineiro",
            "Fluminense": "Fluminense_Football_Club",
            "Botafogo": "Botafogo_de_Futebol_e_Regatas",
            "Vasco da Gama": "Club_de_Regatas_Vasco_da_Gama",
            "São Paulo": "São_Paulo_Futebol_Clube",
            "Santos": "Santos_Futebol_Clube",
            "Cruzeiro": "Cruzeiro_Esporte_Clube",
            "Bahia": "Esporte_Clube_Bahia",
            "Athletico Paranaense": "Club_Athletico_Paranaense",
            "Fortaleza": "Fortaleza_Esporte_Clube",
            "Cuiabá": "Cuiabá_Esporte_Clube",
            "Red Bull Bragantino": "Red_Bull_Bragantino",
            "Juventude": "Esporte_Clube_Juventude",
            "Criciúma": "Criciúma_Esporte_Clube",
            "Vitória": "Esporte_Clube_Vitória",
            "Lanús": "Club_Atlético_Lanús",
            "Belgrano": "Club_Atlético_Belgrano",
            "Argentinos Juniors": "Asociación_Atlética_Argentinos_Juniors",
            "Defensa y Justicia": "Defensa_y_Justicia",
            "Racing Club": "Racing_Club_de_Avellaneda",
            "Boca Juniors": "Club_Atlético_Boca_Juniors",
            "Independiente Medellín": "Independiente_Medellín",
            "América de Cali": "América_de_Cali",
            "Alianza FC": "Alianza_Fútbol_Club_(Colombia)",
            "Deportivo Garcilaso": "Deportivo_Garcilaso",
            "Universidad César Vallejo": "Club_Deportivo_Universidad_César_Vallejo",
            "Sportivo Ameliano": "Club_Sportivo_Ameliano",
            "Nacional Asunción": "Club_Nacional_(Asunción)",
            "Sportivo Luqueño": "Club_Sportivo_Luqueño",
            "Danubio": "Danubio_Fútbol_Club",
            "Montevideo Wanderers": "Montevideo_Wanderers_Fútbol_Club",
            "Racing Club de Montevideo": "Racing_Club_de_Montevideo",
            "Delfín SC": "Delfín_Sporting_Club",
            "Universidad Católica (Ecuador)": "Club_Deportivo_Universidad_César_Vallejo",
            "Unión La Calera": "Unión_La_Calera",
            "Coquimbo Unido": "Coquimbo_Unido",
            "Always Ready": "Club_Always_Ready",
            "Nacional Potosí": "Club_Atlético_Nacional_Potosí",
            "Metropolitanos": "Metropolitanos_Fútbol_Club",
            "Rayo Zuliano": "Deportivo_Rayo_Zuliano"
        },
        "champions": {
            "Real Madrid": "Real_Madrid_Club_de_Fútbol",
            "Barcelona": "Fútbol_Club_Barcelona",
            "Manchester City": "Manchester_City_F.C.",
            "Liverpool": "Liverpool_F.C.",
            "Bayern Munich": "Fußball-Club_Bayern_München",
            "Paris Saint-Germain": "Paris_Saint-Germain_Football_Club",
            "Juventus": "Juventus_Football_Club",
            "Chelsea": "Chelsea_F.C.",
            "Inter": "Football_Club_Internazionale_Milano",
            "AC Milan": "Associazione_Calcio_Milan",
            "Atlético Madrid": "Club_Atlético_de_Madrid",
            "Arsenal": "Arsenal_F.C.",
            "Borussia Dortmund": "Ballspielverein_Borussia_09_e._V._Dortmund",
            "Napoli": "Società_Sportiva_Calcio_Napoli",
            "FC Porto": "Futebol_Clube_do_Porto",
            "Benfica": "Sport_Lisboa_e_Benfica",
            "Sporting CP": "Sporting_Clube_de_Portugal",
            "Ajax": "Amsterdamsche_Football_Club_Ajax",
            "Feyenoord": "Feyenoord_Rotterdam",
            "PSV": "Philips_Sport_Vereniging",
            "Celtic": "Celtic_F.C.",
            "Rangers": "Rangers_F.C.",
            "Shakhtar Donetsk": "FC_Shakhtar_Donetsk",
            "Red Bull Salzburg": "FC_Red_Bull_Salzburg",
            "Lazio": "Società_Sportiva_Lazio",
            "Real Sociedad": "Real_Sociedad_de_Fútbol",
            "Galatasaray": "Galatasaray_S.K._(football)",
            "Fenerbahçe": "Fenerbahçe_S.K._(football)",
            "Copenhagen": "F.C._Copenhagen",
            "Young Boys": "BSC_Young_Boys",
            "Antwerp": "Royal_Antwerp_F.C.",
            "Club Brugge": "Club_Brugge_KV",
            "Red Star Belgrade": "Red_Star_Belgrade",
            "Dinamo Zagreb": "GNK_Dinamo_Zagreb",
            "Olympiacos": "Olympiacos_F.C.",
            "AS Monaco": "AS_Monaco_FC",
            "Lille": "Lille_OSC"
        },
        "saudi": {
            "Al-Nassr": "Al-Nassr_FC",
            "Al-Hilal": "Al-Hilal_SFC",
            "Al-Ittihad": "Al-Ittihad_Club_(Jeddah)",
            "Al-Ahli": "Al-Ahli_Saudi_FC",
            "Al-Ettifaq": "Al-Ettifaq_FC",
            "Al-Shabab": "Al-Shabab_FC_(Riyadh)",
            "Al-Taawoun": "Al-Taawoun_FC",
            "Al-Fateh": "Al-Fateh_SC",
            "Damac": "Damac_FC",
            "Al-Fayha": "Al-Fayha_FC",
            "Al-Tai": "Al-Tai_FC",
            "Al-Raed": "Al-Raed_FC",
            "Abha": "Abha_Club",
            "Al-Khaleej": "Al-Khaleej_FC",
            "Al-Riyadh": "Al-Riyadh_SC",
            "Al-Wehda": "Al-Wehda_FC_Mecca",
            "Al-Hazem": "Al-Hazem_F.C.",
            "Al-Okhdood": "Al-Okhdood_Club",
            "Al-Qadsiah": "Al-Qadsiah_FC",
            "Al-Orobah": "Al-Orobah_F.C."
        },
        "eredivisie": {
            "Ajax": "Amsterdamsche_Football_Club_Ajax",
            "PSV": "Philips_Sport_Vereniging",
            "Feyenoord": "Feyenoord_Rotterdam",
            "AZ Alkmaar": "Alkmaar_Zaanstreek",
            "Utrecht": "Football_Club_Utrecht",
            "Twente": "Football_Club_Twente",
            "Vitesse": "Stichting_Betaald_Voetbal_Vitesse",
            "Heerenveen": "Sportclub_Heerenveen",
            "Go Ahead Eagles": "Go_Ahead_Eagles",
            "Sparta Rotterdam": "Sparta_Rotterdam",
            "Fortuna Sittard": "Fortuna_Sittard",
            "RKC Waalwijk": "RKC_Waalwijk",
            "Excelsior": "Excelsior_Rotterdam",
            "Volendam": "FC_Volendam",
            "Almere City": "Almere_City_FC",
            "Heracles Almelo": "Heracles_Almelo",
            "PEC Zwolle": "PEC_Zwolle",
            "NEC": "N.E.C.",
            "FC Groningen": "FC_Groningen",
            "Willem II": "Willem_II_Tilburg",
            "ADO Den Haag": "ADO_Den_Haag",
            "NAC Breda": "NAC_Breda",
            "FC Emmen": "FC_Emmen",
            "VVV-Venlo": "VVV-Venlo",
            "De Graafschap": "De_Graafschap",
            "Roda JC Kerkrade": "Roda_JC_Kerkrade",
            "MVV Maastricht": "MVV_Maastricht",
            "Telstar": "Telstar",
            "Jong Ajax": "Jong_Ajax",
            "Jong PSV": "Jong_PSV",
            "Jong AZ": "Jong_AZ",
            "TOP Oss": "TOP_Oss",
            "Helmond Sport": "Helmond_Sport",
            "FC Dordrecht": "FC_Dordrecht",
            "Den Bosch": "FC_Den_Bosch",
            "Jong Utrecht": "Jong_FC_Utrecht"
        },
        "argentina": {
            "River Plate": "Club_Atlético_River_Plate",
            "Boca Juniors": "Club_Atlético_Boca_Juniors",
            "Independiente": "Club_Atlético_Independiente",
            "Racing Club": "Racing_Club_de_Avellaneda",
            "San Lorenzo": "Club_Atlético_San_Lorenzo_de_Almagro",
            "Estudiantes": "Estudiantes_de_La_Plata",
            "Gimnasia La Plata": "Gimnasia_y_Esgrima_La_Plata",
            "Rosario Central": "Club_Atlético_Rosario_Central", 
            "Newell's Old Boys": "Club_Atlético_Newell%27s_Old_Boys",
            "Vélez Sarsfield": "Club_Atlético_Vélez_Sarsfield",
            "Lanús": "Club_Atlético_Lanús",
            "Argentinos Juniors": "Asociación_Atlética_Argentinos_Juniors",
            "Union": "Club_Atlético_Unión",
            "Talleres": "Club_Atlético_Talleres",
            "Defensa y Justicia": "Defensa_y_Justicia",
            "Godoy Cruz": "Club_Deportivo_Godoy_Cruz_Antonio_Tomba",
            "Central Córdoba": "Central_Córdoba_de_Santiago_del_Estero",
            "Atlético Tucumán": "Club_Atlético_Tucumán",
            "Platense": "Club_Atlético_Platense",
            "Sarmiento": "Club_Atlético_Sarmiento_(Junín)",
            "Arsenal Sarandí": "Arsenal_Fútbol_Club",
            "Barracas Central": "Club_Atlético_Barracas_Central",
            "Tigre": "Club_Atlético_Tigre",
            "Colón": "Colón_de_Santa_Fe",
            "Huracán": "Club_Atlético_Huracán",
            "Aldosivi": "Club_Atlético_Aldosivi",
            "Patronato": "Club_Atlético_Patronato",
            "Banfield": "Club_Atlético_Banfield",
            "Belgrano": "Club_Atlético_Belgrano",
            "Instituto": "Instituto_Atlético_Central_Córdoba",
            "Independiente Rivadavia": "Club_Sportivo_Independiente_Rivadavia",
            "Deportivo Riestra": "Deportivo_Riestra",
            "Quilmes": "Quilmes_Atlético_Club",
            "Ferro Carril Oeste": "Club_Ferro_Carril_Oeste"
        },
        "mundial-de-clubes": {
            "Al-Ain": "Al-Ain_FC",
            "Al-Hilal": "Al-Hilal_SFC",
            "Urawa Red Diamonds": "Urawa_Red_Diamonds",
            "Ulsan HD": "Ulsan_HD_FC",
            "Al Ahly": "Al_Ahly_SC",
            "Wydad AC": "Wydad_AC",
            "Espérance de Tunis": "Espérance_Sportive_de_Tunis",
            "Mamelodi Sundowns": "Mamelodi_Sundowns_F.C.",
            "Monterrey": "Club_de_Fútbol_Monterrey",
            "Seattle Sounders FC": "Seattle_Sounders_FC",
            "Club León": "Club_León",
            "Pachuca": "C.F._Pachuca",
            "Palmeiras": "Sociedade_Esportiva_Palmeiras",
            "Flamengo": "Clube_de_Regatas_do_Flamengo",
            "Fluminense": "Fluminense_Football_Club",
            "River Plate": "Club_Atlético_River_Plate",
            "Boca Juniors": "Club_Atlético_Boca_Juniors",
            "Chelsea": "Chelsea_F.C.",
            "Real Madrid": "Real_Madrid_Club_de_Fútbol",
            "Manchester City": "Manchester_City_F.C.",
            "Bayern Munich": "Fußball-Club_Bayern_München",
            "Paris Saint-Germain": "Paris_Saint-Germain_Football_Club",
            "Inter": "Football_Club_Internazionale_Milano",
            "FC Porto": "Futebol_Clube_do_Porto",
            "Benfica": "Sport_Lisboa_e_Benfica",
            "Borussia Dortmund": "Ballspielverein_Borussia_09_e._V._Dortmund",
            "Juventus": "Juventus_Football_Club",
            "Atlético Madrid": "Club_Atlético_de_Madrid",
            "FC Red Bull Salzburg": "FC_Red_Bull_Salzburg",
            "Auckland City": "Auckland_City_FC",
            "Oakland Roots": "Oakland_Roots_SC"
        }
    }
    """;
}