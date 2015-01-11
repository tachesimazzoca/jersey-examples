package app.core.view;

import org.junit.Test;

import static org.junit.Assert.*;

public class FormHelperTest {
    public class TestForm {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testGet() {
        TestForm form = new TestForm();
        form.setId("1234");
        form.setName("foo");
        FormHelper<TestForm> helper = new FormHelper<TestForm>(form);

        assertEquals(form.getId(), helper.get("id"));
        assertEquals(form.getName(), helper.get("name"));
    }

    @Test
    public void testToHTMLInput() {
        TestForm form = new TestForm();
        form.setId("");
        form.setName("<foo>");
        FormHelper<TestForm> helper = new FormHelper<TestForm>(form);

        assertEquals("<input type=\"hidden\" name=\"id\" value=\"\">",
                helper.toHTMLInput("hidden", "id"));
        assertEquals("<input type=\"text\" name=\"name\" value=\"&lt;foo&gt;\" disabled>",
                helper.toHTMLInput("text", "name", "disabled"));
    }
}

