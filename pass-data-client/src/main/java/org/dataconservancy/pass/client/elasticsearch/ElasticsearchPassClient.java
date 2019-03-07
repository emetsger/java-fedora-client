/*
 * Copyright 2018 Johns Hopkins University
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

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.http.HttpHost;

import org.dataconservancy.pass.model.PassEntity;
import org.dataconservancy.pass.model.PassEntityType;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Communicates with elasticsearch
 * @author Karen Hanson
 */
public class ElasticsearchPassClient {


    private static final Logger LOG = LoggerFactory.getLogger(ElasticsearchPassClient.class);
    
    /**
     * Template for a search attribute e.g. AND fldname:something
     */
    private static final String QS_ATTRIB_TEMPLATE = " AND %s:%s";
    
    /**
     * Template for a query string e.g. (@type:Submission AND fldname:"something")
     * where the second %s could be one or more QS_ATTRIB_TEMPLATES
     */
    private static final String QS_TEMPLATE = "(@type:%s %s)";

    private static final String EXISTS_TEMPLATE = "_exists_:%s";

    private static final String NOT_EXISTS_TEMPLATE = "-" + EXISTS_TEMPLATE;

    private static final String QS_ATTRIB_NOT_EXISTS_TEMPLATE = " AND " + NOT_EXISTS_TEMPLATE;
    
    private static final String ID_FIELDNAME = "@id";

    /**
     * URL(s) of indexer
     */
    private final HttpHost[] hosts;
        
    /** 
     * Default constructor for PASS client
     */
    public ElasticsearchPassClient() {
        Set<URL> indexerUrls = ElasticsearchConfig.getIndexerHostUrl();      
        hosts = new HttpHost[indexerUrls.size()];
        int count = 0;
        for (URL url : indexerUrls) {
            LOG.info("Connecting to index at {}", url);
            hosts[count] = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
            count = count+1;
        }
        
    }
    
    /**
     * @see org.dataconservancy.pass.client.PassClient#findByAttribute(Class, String, Object)
     * 
     * @param modelClass modelClass
     * @param attribute attribute
     * @param value value
     * @return URI
     * @param <T> PASS entity type
     */
    public <T extends PassEntity> URI findByAttribute(Class<T> modelClass, String attribute, Object value) {
        validateModelParam(modelClass);
        validateAttribValParams(attribute, value, true);
                
        String indexType = null;
        
        if (PassEntityType.getTypeByName(modelClass.getSimpleName())!=null) {
            indexType = PassEntityType.getTypeByName(modelClass.getSimpleName()).getName();
        }

        String attribs = buildAttributeString(attribute, value);
        String querystring = applyTemplate(QS_TEMPLATE, attribs, indexType);
             
        Set<URI> passEntityUris = getIndexerResults(querystring, 2, 0); //get 2 so we can check only one result matched
        if (passEntityUris.size()>1) {
            throw new RuntimeException(
                    format("More than one results was returned by this query (%s = %s). " + 
                            "findByAttribute() searches should match only one result.  Instead found:\n %s", 
                            attribute, value, 
                            join("\n", passEntityUris.stream().map(URI::toString).collect(toList()))));
        }
        URI passEntityUri = null;
        if (passEntityUris.size()>0) {
            passEntityUri = passEntityUris.iterator().next();
        }
        return passEntityUri;
    }

        
    /**
     * @see org.dataconservancy.pass.client.PassClient#findAllByAttribute(Class, String, Object)
     * 
     * @param modelClass modelClass
     * @param attribute attribute
     * @param value value
     * @return Set of URI
     * @param <T> PASS entity type
     */
    public <T extends PassEntity> Set<URI> findAllByAttribute(Class<T> modelClass, String attribute, Object value) {
        return findAllByAttribute(modelClass, attribute, value, ElasticsearchConfig.getIndexerLimit(), 0);
    }
    
        
    /**
     * @see org.dataconservancy.pass.client.PassClient#findAllByAttribute(Class, String, Object, int, int)
     * 
     * @param modelClass modelClass
     * @param attribute attribute
     * @param value value
     * @param limit limit
     * @param offset offset
     * @return Set of URI
     * @param <T> PASS entity type
     */
    public <T extends PassEntity> Set<URI> findAllByAttribute(Class<T> modelClass, String attribute, Object value, int limit, int offset) {
        validateModelParam(modelClass);
        validateAttribValParams(attribute, value, true);
        validLimitOffsetParams(limit, offset);
                
        String indexType = null;
        
        if (PassEntityType.getTypeByName(modelClass.getSimpleName())!=null) {
            indexType = PassEntityType.getTypeByName(modelClass.getSimpleName()).getName();
        }

        String attribs = buildAttributeString(attribute, value);
        String querystring = applyTemplate(QS_TEMPLATE, attribs, indexType);
        Set<URI> passEntityUris = getIndexerResults(querystring, limit, offset);
        
        return passEntityUris;
    }

    
    /**
     *  @see org.dataconservancy.pass.client.PassClient#findAllByAttributes(Class, Map)
     *  
     * @param modelClass modelClass
     * @param valueAttributesMap valueAttributesMap
     * @return Set of URI
     * @param <T> PASS entity type
     */
    public <T extends PassEntity> Set<URI> findAllByAttributes(Class<T> modelClass, Map<String, Object> valueAttributesMap) {
        return findAllByAttributes(modelClass, valueAttributesMap, ElasticsearchConfig.getIndexerLimit(), 0);
    }
    
    
    /**
     * @see org.dataconservancy.pass.client.PassClient#findAllByAttributes(Class, Map, int, int)
     * 
     * @param modelClass modelClass
     * @param valueAttributesMap valueAttributesMap
     * @param limit limit
     * @param offset offset
     * @return Set of URI
     * @param <T> PASS entity type
     */
    public <T extends PassEntity> Set<URI> findAllByAttributes(Class<T> modelClass, Map<String, Object> valueAttributesMap, int limit, int offset) {
        validateModelParam(modelClass);
        validateAttribMapParam(valueAttributesMap);
        validLimitOffsetParams(limit, offset);
        
        LOG.debug("Searching for {} using multiple filters", modelClass.getSimpleName());
        
        String indexType = null;
        
        if (PassEntityType.getTypeByName(modelClass.getSimpleName())!=null) {
            indexType = PassEntityType.getTypeByName(modelClass.getSimpleName()).getName();
        }

        StringBuilder attribs = buildAttributeString(valueAttributesMap);
        String querystring = applyTemplate(QS_TEMPLATE, attribs.toString(), indexType);
                
        Set<URI> passEntityUris = getIndexerResults(querystring, limit, offset);
        return passEntityUris;
    }

    /**
     * Retrieve search results from elasticsearch
     * @param querystring
     * @param limit
     * @param offset
     * @return
     */
    private Set<URI> getIndexerResults(String querystring, int limit, int offset) {
        
        Set<URI> passEntityUris = new HashSet<URI>();
        
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(hosts))){
            
            SearchRequest searchRequest = new SearchRequest(); 
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
            sourceBuilder.from(offset);
            sourceBuilder.size(limit);

            LOG.debug("Searching index using querystring: {}, with limit {} and offset {}", querystring,  limit, offset);
            //(content:this OR name:this)
            QueryStringQueryBuilder matchQueryBuilder = new QueryStringQueryBuilder(querystring);
                        
            matchQueryBuilder.defaultOperator(Operator.AND);
            sourceBuilder.query(matchQueryBuilder);
            searchRequest.source(sourceBuilder);
            SearchResponse searchResponse = client.search(searchRequest);
            SearchHits hits = searchResponse.getHits();
            Iterator<SearchHit> hitsIt = hits.iterator();
            
            while (hitsIt.hasNext()){
                String idField = hitsIt.next().getSourceAsMap().get(ID_FIELDNAME).toString();
                passEntityUris.add(new URI(idField));
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Something was wrong with the record returned from the indexer. The ID could not be recognized as a URI", e);
        } catch (Exception e) {
            throw new RuntimeException(String.format("An error occurred while processing the query: %s", querystring), e);
        }
            
        return passEntityUris;
        
    }
    
    private <T extends PassEntity> void validateAttribMapParam(Map<String,Object> valueAttributesMap) {
        if (valueAttributesMap==null || valueAttributesMap.size()==0) {throw new IllegalArgumentException("valueAttributesMap cannot be empty");}
        for (Entry<String,Object> entry : valueAttributesMap.entrySet()) {
            validateAttribValParams(entry.getKey(), entry.getValue(), true);
        }
    }
    
    private <T extends PassEntity> void validateModelParam(Class<T> modelClass) {
        if (modelClass==null) {throw new IllegalArgumentException("modelClass cannot be null");}
        if (modelClass==PassEntity.class) {throw new IllegalArgumentException("modelClass cannot be the abstract class 'PassEntity.class'");}
    }
    
    private void validLimitOffsetParams(int limit, int offset) {
        if (offset < 0) {throw new IllegalArgumentException("The offset value cannot be less than 0");}       
        if (limit < 0) {throw new IllegalArgumentException("The limit value cannot be less than 0");}        
    }
    
    private void validateAttribValParams(String attribute, Object value, boolean allowNullValues) {
        if (attribute==null || attribute.length()==0) {throw new IllegalArgumentException("attribute cannot be null or empty");}
        if (value instanceof Collection<?>) {throw new IllegalArgumentException("Value for attribute " + attribute + " cannot be a Collection");}
        if (value==null && !allowNullValues) {throw new IllegalArgumentException("Value cannot be null or empty");}
    }

    /**
     * Applies the supplied arguments to the template.
     *
     * @param template
     * @param attribs
     * @param indexType
     * @return
     */
    private static String applyTemplate(String template, String attribs, String indexType) {
        return String.format(template, indexType, attribs);
    }

    /**
     * Prepares the '{@code attribute:value}' portion of the Elastic Search query from a Map of attribute value pairs.
     * <p>
     * Each attribute value pair will be processed according to {@link #buildAttributeString(String, String)}.
     * </p>
     *
     * @param valueAttributesMap a Map keyed by attribute names containing attribute values (which may be {@code null})
     * @return a portion of the Elastic Search query; each {@code non-null} attribute value will be escaped
     * @see #buildAttributeString(String, String)
     */
    static StringBuilder buildAttributeString(Map<String, Object> valueAttributesMap) {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, Object> attr : valueAttributesMap.entrySet()) {
            sb.append(buildAttributeString(attr.getKey(),
                    (attr.getValue() == null) ? null : attr.getValue().toString()));
        }

        return sb;
    }

    /**
     * Prepares the '{@code attribute:value}' portion of the Elastic Search query.
     * <p>
     * If the {@code attributeValue} is {@code null}, then the {@link #QS_ATTRIB_NOT_EXISTS_TEMPLATE} is applied.
     * Otherwise, the {@code attributeValue} is processed to escape any special characters, then has the {@link
     * #QS_ATTRIB_TEMPLATE} applied.
     * </p>
     *
     * @param attributeName  the name of the Elastic Search query attribute
     * @param attributeValue the (pre-escaped) Elastic Search attribute value, or {@code null}
     * @return a portion of the Elastic Search query; escaped in the case of a {@code non-null} attribute value
     */
    static String buildAttributeString(String attributeName, Object attributeValue) {
        return buildAttributeString(attributeName, (attributeValue == null) ? null : attributeValue.toString())
                .toString();
    }

    /**
     * Prepares the '{@code attribute:value}' portion of the Elastic Search query.
     * <p>
     * If the {@code attributeValue} is {@code null}, then the {@link #QS_ATTRIB_NOT_EXISTS_TEMPLATE} is applied.
     * Otherwise, the {@code attributeValue} is processed to escape any special characters, then has the {@link
     * #QS_ATTRIB_TEMPLATE} applied.
     * </p>
     *
     * @param attributeName the name of the Elastic Search query attribute
     * @param attributeValue the (pre-escaped) Elastic Search attribute value, or {@code null}
     * @return a portion of the Elastic Search query; escaped in the case of a {@code non-null} attribute value
     */
    static StringBuilder buildAttributeString(String attributeName, String attributeValue) {
        StringBuilder sb = new StringBuilder();
        if (attributeValue != null) {
            StringBuilder escapedValue = new StringBuilder(attributeValue);
            escape(escapedValue);
            sb.append(applyTemplate(QS_ATTRIB_TEMPLATE, escapedValue.toString(), attributeName));
        } else {
            sb.append(String.format(QS_ATTRIB_NOT_EXISTS_TEMPLATE, attributeName));
        }

        return sb;
    }

    /**
     * Escapes special characters in the supplied string.  The supplied string should <em>not</em> be escaped already-
     * there are no provisions in this method for detecting already-escaped strings.  If a string already contains
     * escapes, it will be doubly-escaped.
     * <p>
     * This method simply returns the same StringBuilder it was supplied, as a convenience to the caller.
     * </p>
     *
     * @param valueString the string which may contain special characters requiring escape
     * @return the string with any special characters escaped
     */
    private static StringBuilder escape(StringBuilder valueString) {
        // https://www.elastic.co/guide/en/elasticsearch/reference/6.2/query-dsl-query-string-query.html#_reserved_characters
        // + - = && || > < ! ( ) { } [ ] ^ " ~ * ? : \ /
//        Stream.of("\\", "+", "-", "=", "&&", "||", ">", "<", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "/")
        Stream.of(":", "/")
                .forEach((lookingFor) ->
                    {
                        String replacement = join("", "\\", lookingFor);
//                        System.err.println(">> replacement '" + replacement + "'");
                        replace(valueString.indexOf(lookingFor), lookingFor, replacement, valueString);
                    });
        return valueString;
    }

    /**
     * Recursively searches the {@code builder} for the string {@code lookingFor} and replacing it with {@code
     * replacement}.
     *
     * @param idx the index of {@code lookingFor} in {@code builder}, may be {@code -1}
     * @param lookingFor the string being replaced in the {@code builder}
     * @param replacement the string replacing {@code lookingFor}
     * @param builder the string that may contain {@code lookingFor}
     * @return -1
     */
    private static int replace(int idx, String lookingFor, String replacement, StringBuilder builder) {
        if (idx > -1 && idx < builder.length()) {
//            System.err.println(">> replacing character at index " + idx + " ('" + builder.charAt(idx) + "') with '" + replacement + "'");
            builder.replace(idx, idx + 1, replacement);
            return replace(builder.indexOf(lookingFor, idx + 2), lookingFor, replacement, builder);
        } else {
            return -1;
        }
    }
    
}
