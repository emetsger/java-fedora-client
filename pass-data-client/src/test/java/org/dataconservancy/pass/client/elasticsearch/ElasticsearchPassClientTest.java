/*
 * Copyright 2019 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataconservancy.pass.client.elasticsearch;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.dataconservancy.pass.client.elasticsearch.ElasticsearchPassClient.buildAttributeString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
public class ElasticsearchPassClientTest {

    private static final String DEFAULT_OP = " AND ";

    private static final String DOESNT_EXIST_OP = "-_exists_";

    @Test
    public void colonEscapeAsMap() {
        StringBuilder result = buildAttributeString(new HashMap<String, Object>() {
            {
                put("field", "val:ue");
            }
        });

        assertEquals("Expected ':' to be escaped in attribute string '" + result + "'",
                1, occurs("\\:", result.toString()));
        assertEquals(DEFAULT_OP + "field:val\\:ue", result.toString());
    }

    @Test
    public void colonEscape() {
        StringBuilder result = buildAttributeString("field", "val:ue");

        assertEquals("Expected ':' to be escaped in attribute string '" + result + "'",
                1, occurs("\\:", result.toString()));
        assertEquals(DEFAULT_OP + "field:val\\:ue", result.toString());
    }

    @Test
    public void adjacentColonEscapeAsMap() {
        StringBuilder result = buildAttributeString(new HashMap<String, Object>() {
            {
                put("field", "val::ue");
            }
        });

        assertEquals("Expected every ':' to be escaped in attribute string '" + result + "'",
                2, occurs("\\:", result.toString()));
        assertEquals(DEFAULT_OP + "field:val\\:\\:ue", result.toString());
    }

    @Test
    public void adjacentColonEscape() {
        StringBuilder result = buildAttributeString("field", "val::ue");

        assertEquals("Expected every ':' to be escaped in attribute string '" + result + "'",
                2, occurs("\\:", result.toString()));
        assertEquals(DEFAULT_OP + "field:val\\:\\:ue", result.toString());
    }

    @Test
    public void beginningColonEscapeAsMap() {
        StringBuilder result = buildAttributeString(new HashMap<String, Object>() {
            {
                put("field", ":value");
            }
        });

        assertEquals("Expected ':' to be escaped in attribute string '" + result + "'",
                1, occurs("\\:", result.toString()));
        assertEquals(DEFAULT_OP + "field:\\:value", result.toString());
    }

    @Test
    public void endingColonEscapeAsMap() {
        StringBuilder result = buildAttributeString(new HashMap<String, Object>() {
            {
                put("field", "value:");
            }
        });

        assertTrue("Expected ':' to be escaped in attribute string '" + result + "'",
                result.toString().endsWith("\\:"));
        assertEquals(DEFAULT_OP + "field:value\\:", result.toString());
    }

    @Test
    public void multipleColonEscapeAsMap() {
        StringBuilder result = buildAttributeString(new HashMap<String, Object>() {
            {
                put("field", "v:al:ue");
            }
        });

        assertEquals("Expected every ':' to be escaped in attribute string '" + result + "'",
                2, occurs("\\:", result.toString()));
        assertEquals(DEFAULT_OP + "field:v\\:al\\:ue", result.toString());
    }

    @Test
    public void noEscapeAsMap() {
        StringBuilder result = buildAttributeString(new HashMap<String, Object>() {
            {
                put("field", "value");
            }
        });

        assertFalse("Expected non-escaped attribute string '" + result + "'",
                result.toString().contains("\\:"));
        assertEquals("Expected non-escaped attribute string '" + result + "'",
                0, occurs("\\:", result.toString()));
        assertEquals(DEFAULT_OP + "field:value", result.toString());
    }

    @Test
    public void alreadyEscapedAsMap() {
        StringBuilder result = buildAttributeString(new HashMap<String, Object>() {
            {
                put("field", "val\\:ue");
            }
        });

        assertTrue("Expected : to be doubly-escaped in attribute string '" + result + "'",
                result.toString().contains("\\\\:"));
        assertEquals(DEFAULT_OP + "field:val\\\\:ue", result.toString());
    }

    @Test
    public void passEntityIdAsMap() {
        StringBuilder result = buildAttributeString(new HashMap<String, Object>() {
            {
                put("@id", "http://fcrepo:8080/fcrepo/rest/submissions/ab/cd/ef/abcdef");
            }
        });

        assertEquals("Expected every ':' to be escaped in attribute value '" + result + "'",
                2, occurs("\\:", result.toString()));
        assertEquals("Expected every '/' to be escaped in attribute value '" + result + "'",
                9, occurs("\\/", result.toString()));
        assertEquals(DEFAULT_OP + "@id:http\\:\\/\\/fcrepo\\:8080\\/fcrepo\\/rest\\/submissions\\/ab\\/cd\\/ef\\/abcdef", result.toString());
    }

    @Test
    public void attributeMapContainingNull() {
        StringBuilder result = buildAttributeString(new HashMap<String, Object>() {
            {
                put("foo", "bar");
                put("biz", null);
                put("baz", "foo");
            }
        });

        assertEquals(DEFAULT_OP + DOESNT_EXIST_OP + ":biz" + DEFAULT_OP + "foo:bar" + DEFAULT_OP + "baz:foo",
                result.toString());
    }

    @Test
    public void buildAttributeStringFromMap() {
        Map<String, Object> attrMap = new HashMap<String, Object>() {
            {
                put("foo", "bar");
                put("baz", "biz");
            }
        };

        StringBuilder result = buildAttributeString(attrMap);

        assertEquals(2, occurs("AND", result.toString()));
        assertEquals(1, occurs("foo:bar", result.toString()));
        assertEquals(1, occurs("baz:biz", result.toString()));
        assertEquals(DEFAULT_OP + "foo:bar" + DEFAULT_OP + "baz:biz", result.toString());
    }

    @Test
    public void buildAndEscapeAttributeStringFromMap() {
        Map<String, Object> attrMap = new HashMap<String, Object>() {
            {
                put("foo", "b:ar");
                put("baz", "[biz]");
            }
        };

        StringBuilder result = buildAttributeString(attrMap);

        assertEquals(result.toString(), 2, occurs("AND", result.toString()));
        assertEquals(result.toString(), 1, occurs("foo:b\\:ar", result.toString()));
        assertEquals(result.toString(), 1, occurs("baz:[biz]", result.toString()));
        assertEquals(DEFAULT_OP + "foo:b\\:ar" + DEFAULT_OP + "baz:[biz]", result.toString());
    }

    private static int occurs(String needle, String haystack) {
        int idx = -1;
        int occurs = 0;

        do {
            idx = haystack.indexOf(needle, idx + 1);
            occurs++;
        } while (idx > -1);

        return occurs == -1 ? 0 : occurs - 1;
    }
}