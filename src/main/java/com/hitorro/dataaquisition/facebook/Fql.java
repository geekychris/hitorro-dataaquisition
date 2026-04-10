/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.dataaquisition.facebook;

import com.hitorro.util.sqllike.SqlLike;


public class Fql extends SqlLike<Fql> {
    public static final String StreamFields[] = {"post_id", "message", "comments", "attachment", "action_links", "likes", "type",
            "app_id", "actor_id", "source_id", "target_id", "updated_time", "created_time", "permalink"};
    public static final String StatusFields[] = {"status_id", "message"};
    public static final String CommentsFields[] = {"text", "time", "fromid", "id"};
    public static final String PageFields[] = {"name", "page_id", "pic_square", "pic_big", "pic_small", "pic", "pic_large",
            "page_url", "type", "website", "founded", "company_overview", "products", "location",
            "public_transit", "fan_count"};
    public static final String ConnectionFields[] = {"source_id", "target_id", "target_type", "is_following", "update_time", "is_deleted"};
    protected static String root = "http://api.facebook.com/method/fql.query?query=";
    protected static String rootS = "https://api.facebook.com/method/fql.query?query=";


    public Fql() {
        super(null);
    }

    public String getRoot() {
        return root;
    }

    public String getRootS() {
        return rootS;
    }


    /**
     * POSTS
     * http://api.facebook.com/method/fql.query?query=select post_id, message, comments, attachment, action_links, likes, type, app_id,actor_id, source_id, target_id, updated_time, created_time, permalink from stream where source_id = '73340321070' and created_time > 1291513056 and created_time < 1291561222 ORDER BY created_time ASC limit 125
     * http://api.facebook.com/method/fql.query?query=select post_id, message, comments, attachment, action_links, likes, type, app_id,actor_id, source_id, target_id, updated_time, created_time, permalink from stream where source_id = '73340321070' ORDER BY created_time ASC limit 125
     * comments for a post?
     *
     * COMMENT
     * https://api.facebook.com/method/fql.query?query=select text, time, fromid, id from comment where post_id = '113385204488_493817264488' and time > 1292022112 and time < 1292044195 ORDER BY time ASC limit 1000,999&access_token=172845802731326|b086cfdf37930997685075af-100001811139615|dAPB6efzVzy248bZXOz78qTVe0U
     *
     * PAGE
     * select name, page_id, pic_square, pic_big, pic_small, pic, pic_large, page_url, type, website, founded, company_overview, products, location, " +
     " public_transit, fan_count from page where page_id in ( %s )
     * USER
     * select uid, first_name, last_name, middle_name, name, pic, pic_big, pic_square,pic_small, profile_update_time, sex, username from user where uid in (%s)
     */
}
