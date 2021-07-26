package org.searchengine.entities;


import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(name = "TOKEN",
       indexes = { @Index(name="token_idx", columnList = "text", unique = false),
                   @Index(name="lemma_idx", columnList = "lemma", unique = false),
                   @Index(name="soundex_idx", columnList = "soundexcode", unique = false),
                   @Index(name="metaphone_idx", columnList = "metaphonecode", unique = false)
       })
public class BasicToken implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tokseq_generator")
    @SequenceGenerator(name="tokseq_generator", sequenceName = "TOK_SEQ", allocationSize=1)
    private long id;

    @Column(name="startchar", nullable = false)
    private int startchar;

    @Column(name="endchar", nullable = false)
    private int endchar;

    @Column(name="text", nullable = false)
    private String text;

    @Column(name="lemma", nullable = false)
    private String lemma;

    @Column(name="pos", nullable = false)
    private String pos;

    @Column(name="soundexcode", nullable = false)
    private String soundexcode;

    @Column(name="metaphonecode", nullable = false)
    private String metaphonecode;

    @ManyToOne
    @JoinColumn(name="parentdoc_id", referencedColumnName="id", nullable = false, updatable = false, insertable = true)
    private SimpleDocument parentdoc;    


    public BasicToken() {
      //no-args constructor required by jpa
    }

    public BasicToken(int startchar, int endchar,
                      String text, String lemma, String pos, String soundexcode, String metaphonecode,
                      SimpleDocument doc) {
        this.startchar = startchar;
        this.endchar = endchar;
        this.text = text;
	    this.lemma = lemma;
        this.pos = pos;
        this.soundexcode = soundexcode;
        this.metaphonecode = metaphonecode;
        this.parentdoc = doc;
    }

    public void setId(long id) {

        this.id = id;
    }

    public long getId() {

        return id;
    }

    public void setStartchar(int startchar) {

        this.startchar = startchar;
    }

    public int getStartchar() {

        return startchar;
    }

    public void setEndchar(int endchar) {

        this.endchar = endchar;
    }

    public int getEndchar() {

        return endchar;
    }

    public void setText(String text) {

        this.text = text;
    }

    public String getText() {

        return text;
    }

    public void setLemma(String lemma) {

        this.lemma = lemma;
    }

    public String getLemma() {

        return lemma;
    }

    public void setPos(String pos) {

        this.pos = pos;
    }

    public String getPos() {

        return pos;
    }

    public void setSoundexcode(String soundexcode) {

        this.soundexcode = soundexcode;
    }

    public String getSoundexcode() {

        return soundexcode;
    }

    public void setMetaphonecode(String metaphonecode) {

        this.metaphonecode = metaphonecode;
    }

    public String getMetaphonecode() {

        return metaphonecode;
    }

    public void setParentDocument(SimpleDocument doc){

        this.parentdoc = doc;
    }

    public SimpleDocument getParentDoc(){

        return this.parentdoc;
    }



    @Override
    public String toString() {
        String template = "Token[ID=%d, FROM=%d TO=%d, TEXT=%s, LEMMA=%s, SOUNDEX=%s, METAPHONE=%s, counter=%d, docid=%d]";
        String actualStr = String.format(template, this.id, this.startchar, this.endchar,
                                        this.text, this.lemma, this.soundexcode, this.metaphonecode,
                                        this.parentdoc);
        return actualStr;        
    }
}
