package org.jabref.model.entry;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jabref.model.database.BibDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class BibEntryTest {

    private BibEntry entry;

    @Before
    public void setUp() {
        entry = new BibEntry();
    }

    @After
    public void tearDown() {
        entry = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void notOverrideReservedFields() {
        entry.setField(BibEntry.ID_FIELD, "somevalue");
    }

    @Test(expected = IllegalArgumentException.class)
    public void notClearReservedFields() {
        entry.clearField(BibEntry.ID_FIELD);
    }

    @Test
    public void getFieldIsCaseInsensitive() throws Exception {
        entry.setField("TeSt", "value");

        assertEquals(Optional.of("value"), entry.getField("tEsT"));
    }

    @Test
    public void clonedBibentryHasUniqueID() throws Exception {
        BibEntry entry = new BibEntry();
        BibEntry entryClone = (BibEntry) entry.clone();

        assertNotEquals(entry.getId(), entryClone.getId());
    }

    @Test
    public void testGetAndAddToLinkedFileList() {
        List<LinkedFile> files = entry.getFiles();
        files.add(new LinkedFile("", "", ""));
        entry.setFiles(files);
        assertEquals(Arrays.asList(new LinkedFile("", "", "")), entry.getFiles());
    }

    @Test
    public void testGetResolvedFieldOrAliasThis() throws Exception {
        BibDatabase database = new BibDatabase();
        entry.setField(FieldName.AUTHOR, "entryAuthor");

        assertEquals("entryAuthor", entry.getResolvedFieldOrAlias(FieldName.AUTHOR, database).get());
    }

    @Test
    public void testGetResolvedFieldOrAliasThisAndCrossref() throws Exception {
        BibDatabase database = new BibDatabase();
        BibEntry entry2 = new BibEntry();
        entry.setField(FieldName.CROSSREF, "entry2");
        entry.setField(FieldName.AUTHOR, "entry1Author");
        entry2.setCiteKey("entry2");
        entry2.setField(FieldName.AUTHOR, "entry2Author");
        database.insertEntry(entry2);
        database.insertEntry(entry);

        assertEquals("entry1Author", entry.getResolvedFieldOrAlias(FieldName.AUTHOR, database).get());
    }

    @Test
    public void testGetResolvedFieldOrAliasCrossref() throws Exception {
        BibDatabase database = new BibDatabase();
        BibEntry entry2 = new BibEntry();
        entry.setField(FieldName.CROSSREF, "entry2");
        entry2.setCiteKey("entry2");
        entry2.setField(FieldName.AUTHOR, "entryAuthor");
        database.insertEntry(entry2);
        database.insertEntry(entry);

        assertEquals("entryAuthor", entry.getResolvedFieldOrAlias(FieldName.AUTHOR, database).get());
    }

    @Test
    public void testGetResolvedFieldOrAliasDoubleCrossref() throws Exception {
        BibDatabase database = new BibDatabase();
        BibEntry entry2 = new BibEntry();
        BibEntry entry3 = new BibEntry();
        entry.setField(FieldName.CROSSREF, "entry2");
        entry2.setField(FieldName.CROSSREF, "entry3");
        entry2.setCiteKey("entry2");
        entry3.setCiteKey("entry3");
        entry3.setField(FieldName.AUTHOR, "entryAuthor");
        database.insertEntry(entry3);
        database.insertEntry(entry2);
        database.insertEntry(entry);

        assertEquals("entryAuthor", entry.getResolvedFieldOrAlias(FieldName.AUTHOR, database).get());
    }

    @Test
    public void testGetResolvedFieldOrAliasDecaCrossref() throws Exception {
        BibDatabase database = new BibDatabase();
        database.insertEntry(entry);
        BibEntry last = entry;
        for (int i = 2; i < 11; i++) {
            String citeKey = "entry" + i;
            BibEntry current = new BibEntry();
            current.setCiteKey(citeKey);
            last.setField(FieldName.CROSSREF, citeKey);
            database.insertEntry(current);
            last = current;
        }
        last.setField(FieldName.AUTHOR, "entryAuthor");

        assertEquals("entryAuthor", entry.getResolvedFieldOrAlias(FieldName.AUTHOR, database).get());
    }
}
