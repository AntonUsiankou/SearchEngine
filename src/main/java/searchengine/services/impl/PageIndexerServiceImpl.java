package searchengine.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexSearch;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexSearchRepository;
import searchengine.repository.LemmaRepository;
import searchengine.services.LemmaService;
import searchengine.services.PageIndexerService;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PageIndexerServiceImpl implements PageIndexerService {
    private final LemmaService lemmaService;
    private final LemmaRepository lemmaRepository;
    private final IndexSearchRepository indexSearchRepository;

    @Override
    public void indexHtml(String html, Page indexingPage) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Integer> lemmas = lemmaService.getLemmasFromText(html);
            lemmas.entrySet().parallelStream().forEach(entry -> saveLemma(entry.getKey(), entry.getValue(), indexingPage));
            log.debug("Индексация страницы: " + (System.currentTimeMillis() - start) + "lemmas: " + lemmas.size());
        } catch (IOException e) {
            log.error(String.valueOf(e));
            throw new RuntimeException(e);
        }
    }

    @Override
    public void refreshIndex(String html, Page refreshingPage) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Integer> lemmas = lemmaService.getLemmasFromText(html);
            //уменьшить frequency у лемм, которые присутствуют на обновляемой странице
            refreshLemma(refreshingPage);
            //удаление индекса
            indexSearchRepository.deleteIndexByPageId(refreshingPage.getId());
            //обновление лемм и индексов у обновленной страницы
            lemmas.entrySet().parallelStream().forEach(entry -> saveLemma(entry.getKey(), entry.getValue(), refreshingPage));
            log.debug("Обновление индекса страницы: " + (System.currentTimeMillis() - start) + "lemmas: " + lemmas.size());
        } catch (IOException e) {
            log.error(String.valueOf(e));
            throw new RuntimeException(e);
        }
    }

    @Transactional
    protected void refreshLemma(Page refreshPage) {
        List<IndexSearch> indexes = indexSearchRepository.findAllByPageId(refreshPage.getId());
        indexes.forEach(idx -> {
            Optional<Lemma> lemmaToRefresh = lemmaRepository.findById(idx.getLemmaId());
            lemmaToRefresh.ifPresent(lemma -> {
                lemma.setFrequency(lemma.getFrequency() - idx.getLemmaCount());
                lemmaRepository.saveAndFlush(lemma);
            });
        });
    }

    @Transactional
    protected void saveLemma(String k, Integer v, Page indexingPage) {
        Lemma existLemmaInDb = lemmaRepository.lemmaExist(k, indexingPage.getSiteId());
        if (existLemmaInDb != null) {
            existLemmaInDb.setFrequency(existLemmaInDb.getFrequency() + v);
            lemmaRepository.saveAndFlush(existLemmaInDb);
            createIndex(indexingPage, existLemmaInDb, v);
        } else {
            try {
                Lemma newLemmaToDb = new Lemma();
                newLemmaToDb.setSiteId(indexingPage.getSiteId());
                newLemmaToDb.setLemma(k);
                newLemmaToDb.setFrequency(v);
                newLemmaToDb.setSitePage(indexingPage.getSitePage());
                lemmaRepository.save(newLemmaToDb);
                createIndex(indexingPage, newLemmaToDb, v);
            } catch (DataIntegrityViolationException ex) {
                log.debug("Ошибка при сохранении леммы, такая лемма уже существует. Вызов повторного сохранения");
                saveLemma(k, v, indexingPage);
            }
        }
    }

    private void createIndex(Page indexingPage, Lemma lemmaInDb, Integer rank) {
        IndexSearch indexSearchExist = indexSearchRepository.indexSearchExist(indexingPage.getId(), lemmaInDb.getId());
        if (indexSearchExist != null) {
            indexSearchExist.setLemmaCount(indexSearchExist.getLemmaCount() + rank);
            indexSearchRepository.save(indexSearchExist);
        } else {
            IndexSearch index = new IndexSearch();
            index.setPageId(indexingPage.getId());
            index.setLemmaId(lemmaInDb.getId());
            index.setLemmaCount(rank);
            index.setLemma(lemmaInDb);
            index.setPage(indexingPage);
            indexSearchRepository.save(index);
        }
    }
}
