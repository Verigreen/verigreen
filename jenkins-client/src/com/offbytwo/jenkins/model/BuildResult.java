/*
 * Copyright 2013, 2014013 Rising Oak LLC.
 *
 * Distributed under the MIT license: http://opensource.org/licenses/MIT
 */

package com.offbytwo.jenkins.model;

public enum BuildResult {
    FAILURE,
    UNSTABLE,
    REBUILDING,
    BUILDING,
    ABORTED,
    SUCCESS,
    UNKNOWN
}
