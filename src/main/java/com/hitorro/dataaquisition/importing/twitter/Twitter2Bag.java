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
package com.hitorro.dataaquisition.importing.twitter;

import com.hitorro.dataaquisition.importing.mapping.TupleSet;
import com.hitorro.dataaquisition.importing.mapping.json2bag.JBContainingTupleMap;
import com.hitorro.dataaquisition.importing.mapping.json2bag.JBTupleCombiner;
import com.hitorro.dataaquisition.importing.mapping.json2bag.JBTupleSet;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.opers.AlwaysTrueOperator;
import com.hitorro.util.core.opers.LogicalSelector;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.json.operators.JSONFieldExists;
import com.hitorro.util.typesystem.Bag;

/**
 * Map a JSON element tree that represents a tweet or a delete of a tweet into a bag using Tuple Mappers to do its
 * bidding.
 */
public class Twitter2Bag {

    private static Object TwitterRootMappings[][] = {
            {"created_at", "created", JSON2BagMapperViaTupleSelector.jsonDateToLong},
            {"id", "id"},
            {"text", "body"},
            {"retweeted", "retweeted"},
    };
    private static Object UserMappings[][] = {{"name", "username"},
            {"id", "twitterid"},
            {"screen_name", "screenname"},
            {"location", "location"},
            {"profile_image_url", "avatarurl"},
            {"language", "lang"},
            {"description", "userdescription"},
            {"time_zone", "usertimezone"},
            {"favorites_count", "favoritescount"},
            {"statuses_count", "statuscount"},
            {"followers_count", "followerscount"},
            {"friends_count", "friendscount"}
    };

    private static Object StatusDelete[][] = {{"user_id", "userid"},
            {"id", "twitterid"},

    };

    public static Mapper<JSONElement, Bag> twitter2bag = new JSON2BagMapperViaTupleSelector(getSelector());


    /**
     * pull out of the tweet some basic tweet info.
     *
     * @return
     */
    protected static TupleSet getTwitterMessageMappingTupleSet() {
        TupleSet userMap = new JBTupleSet();
        userMap.add(UserMappings);
        JBContainingTupleMap container = new JBContainingTupleMap("user", userMap);
        TupleSet ts = new JBTupleSet();
        ts.add(container);
        ts.add(TwitterRootMappings);
        // combine the screen name and id together to give a unique url
        ts.add(new JBTupleCombiner(new String[]{"user.screen_name", "id"}, new Mapper[]{null, null}, "http://twitter.com/%s/statuses/%s", "url"));
        ts.finishSetup();
        return ts;
    }

    /**
     * we care about pulling out two values under "delete.status".
     *
     * @return
     */
    protected static TupleSet getTwitterDeleteMappingTupleSet() {
        TupleSet deleteMap = new JBTupleSet();
        deleteMap.add(StatusDelete);
        JBContainingTupleMap container = new JBContainingTupleMap("delete.status", deleteMap);
        container.finishSetup();
        TupleSet ts = new JBTupleSet();
        ts.add(container);
        ts.finishSetup();
        return ts;
    }

    /**
     * Selector that given a JSONElement representing some kind of tweet will decide which Tuple me to use at the
     * moment that is either a regular twitter document or its delete object
     *
     * @return
     */
    protected static LogicalSelector<JSONElement, GenericKeyValue<String, TupleSet>> getSelector() {
        LogicalSelector<JSONElement, GenericKeyValue<String, TupleSet>> ls = new LogicalSelector();
        ls.addSelection(new JSONFieldExists("delete"), new GenericKeyValue<String, TupleSet>("twitterdelete", getTwitterDeleteMappingTupleSet()));
        ls.addSelection(AlwaysTrueOperator.oper, new GenericKeyValue<String, TupleSet>("twitter", getTwitterMessageMappingTupleSet()));
        ls.finishSetup();
        return ls;
    }

}





