package org.searchengine.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

import org.apache.commons.codec.language.Soundex;
import org.apache.commons.codec.language.Metaphone;

import java.util.*;
import java.util.Optional;
import org.javatuples.*;
import java.util.stream.Collectors;

import org.searchengine.repositories.DocumentRepository;
import org.searchengine.repositories.DocumentCollectionRepository;
import org.searchengine.repositories.BasicTokenRepository;

import org.searchengine.entities.SimpleDocument;
import org.searchengine.entities.DocumentCollection;
import org.searchengine.entities.BasicToken;


@Service
public class IngestionService {
     
    @Autowired
    private DocumentRepository docRepo;

    @Autowired
    private DocumentCollectionRepository doccollRepo;

    @Autowired
    private BasicTokenRepository tokensRepo;

    @Autowired
    private IndexingService idx;

    private StanfordCoreNLP pipeline;
    private Metaphone mtph;
    private Soundex sdx;

    public IngestionService(){
        // --- related to corenlp
        //disable logging 
        RedwoodConfiguration.empty2();    

        Properties props = new Properties();
        // set the list of annotators to run - tokenizer and sentences splits
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma"); 
        props.setProperty("tokenize.options", "splitHyphenated=false,americanize=false");
        //pos tagger needs model (15 MB) from https://nlp.stanford.edu/software/tagger.shtml#Download stored in resources folder
        props.setProperty("pos.model", "english-left3words-distsim.tagger");

        this.pipeline = new StanfordCoreNLP(props);

        this.sdx = new Soundex();
        this.mtph = new Metaphone();
        this.mtph.setMaxCodeLen(10);
    }
  
    public void createCollection(String name){

        DocumentCollection doccoll = new DocumentCollection(name);
        doccollRepo.saveAndFlush(doccoll);
    }

    public void insertCollection(String collectionName, List<String> payload){
      
        Optional<DocumentCollection> doccollOpt = doccollRepo.findById(collectionName);
        if(doccollOpt.isPresent()) {
            DocumentCollection doccoll = doccollOpt.get();

            for(String txt : payload){
                SimpleDocument doc = new SimpleDocument(txt, doccoll);
                SimpleDocument docsav = docRepo.saveAndFlush(doc);

                //parse and insert tokens
                List<BasicToken> tokensList = tokenizeDocument(docsav);
                tokensRepo.saveAll(tokensList);

                //index also in the patriciatrie
                indexTokens(tokensList, docsav);
            }
        }
     
    }

    public void indexTokens(List<BasicToken> tokensList, SimpleDocument doc){

        //stream the incoming list and grou by token text, counting occurences
        Map<String, Long> countsByRawText = tokensList.stream()
                                                      .map(e-> e.getText())
                                                      .collect(Collectors.groupingBy(e-> e, Collectors.counting()));

        //stream the incoming list and grou by token lemma, counting occurences
        Map<String, Long> countsByLemma = tokensList.stream()
                                                    .map(e-> e.getLemma())
                                                    .collect(Collectors.groupingBy(e-> e, Collectors.counting()));

        //add to index
        countsByRawText.entrySet().stream().forEach(e -> this.idx.indexTokenTextDoc(e.getKey(), doc, e.getValue()));
        countsByLemma.entrySet().stream().forEach(e -> this.idx.indexTokenLemmaDoc(e.getKey(), doc, e.getValue()));
    }

    public Pair<String,String> pronounciationToken(String s){
        Pair<String,String> res = new Pair<String,String>(this.sdx.encode(s), this.mtph.encode(s));
        return res;
    }

    public List<BasicToken> tokenizeDocument(SimpleDocument doc){
        int numTokens = 0;
        List<BasicToken> tokensFiltered = new ArrayList<BasicToken>();
        String punctuations = ".,;:!?\"()”-“–";

        /**selection of part of speech of stopwords to be removed
        *  see https://stackoverflow.com/questions/1833252/java-stanford-nlp-part-of-speech-labels
        *
        * CC: conjunction, coordinating
        *     & 'n and both but either et for less minus neither nor or plus so
        *     therefore times v. versus vs. whether yet
        * CD: numeral, cardinal
        *     mid-1890 nine-thirty forty-two one-tenth ten million 0.5 one forty-
        *     seven 1987 twenty '79 zero two 78-degrees eighty-four IX '60s .025
        *     fifteen 271,124 dozen quintillion DM2,000 ...
        * DT: determiner
        *     all an another any both del each either every half la many much nary
        *     neither no some such that the them these this those
        * EX: existential there
        *     there
        * IN: preposition or conjunction, subordinating
        *     astride among uppon whether out inside pro despite on by throughout
        *     below within for towards near behind atop around if like until below
        *     next into if beside ...
        * PDT: pre-determiner
        *     all both half many quite such sure this
        * POS: genitive marker
        *     ' 's
        * RP: particle
        *     aboard about across along apart around aside at away back before behind
        *     by crop down ever fast for forth from go high i.e. in into just later
        *     low more off on open out over per pie raising start teeth that through
        *     under unto up up-pp upon whole with
        * TO: "to" as preposition or infinitive marker
        *     to
        * UH Interjection
        * WDT: WH-determiner
        *     that what whatever which whichever
        * WP: WH-pronoun
        *     that what whatever whatsoever which who whom whosoever
        * WP$: WH-pronoun, possessive
        *     whose
        * WRB: Wh-adverb
        *     how however whence whenever where whereby whereever wherein whereof why
        *
        */
        List<String> pos_stopwords = Arrays.asList( "CC", "CD", "DT", "EX", "IN", "PDT", "POS", "RP", "TO", "UH", "WDT", "WP", "WP$", "WRB");

        CoreDocument nlpdoc = new CoreDocument(doc.getText());
        this.pipeline.annotate(nlpdoc);

        //iterate through the tokens and remove punctuation and stopwords
        for (CoreLabel tok : nlpdoc.tokens()) {
            if ((tok.tag().equalsIgnoreCase("SYM") == false)
                && (tok.tag().equals(".") == false)
                && (punctuations.contains(tok.word()) == false) ) {
                //numtokens counts all non-punctuation tokens
                numTokens += 1;

                if (pos_stopwords.contains(tok.tag()) == false) {

                    Pair<String, String> tokenAudio = pronounciationToken(tok.word());

                    tokensFiltered.add(new BasicToken(tok.beginPosition(), tok.endPosition(),
                                                      tok.word().toLowerCase(), tok.lemma().toLowerCase(), tok.tag(),
                                                      tokenAudio.getValue0(), tokenAudio.getValue1(), doc ));
                }

            }
        }

        //set on document the number of tokens that are not punctuation
        doc.setNumtokens(numTokens);
        //set on documnet the number of tokens after filtering out the stopwords
        doc.setNumFilteredTokens(tokensFiltered.size());

        return tokensFiltered;
    }

}
