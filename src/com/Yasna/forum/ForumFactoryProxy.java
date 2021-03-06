/**
 * Copyright (C) 2001 Yasna.com. All rights reserved.
 *
 * ===================================================================
 * The Apache Software License, Version 1.1
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Yasna.com (http://www.yasna.com)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Yazd" and "Yasna.com" must not be used to
 *    endorse or promote products derived from this software without
 *    prior written permission. For written permission, please
 *    contact yazd@yasna.com.
 *
 * 5. Products derived from this software may not be called "Yazd",
 *    nor may "Yazd" appear in their name, without prior written
 *    permission of Yasna.com.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL YASNA.COM OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Yasna.com. For more information
 * on Yasna.com, please see <http://www.yasna.com>.
 */

/**
 * Copyright (C) 2000 CoolServlets.com. All rights reserved.
 *
 * ===================================================================
 * The Apache Software License, Version 1.1
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        CoolServlets.com (http://www.coolservlets.com)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Jive" and "CoolServlets.com" must not be used to
 *    endorse or promote products derived from this software without
 *    prior written permission. For written permission, please
 *    contact webmaster@coolservlets.com.
 *
 * 5. Products derived from this software may not be called "Jive",
 *    nor may "Jive" appear in their name, without prior written
 *    permission of CoolServlets.com.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL COOLSERVLETS.COM OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of CoolServlets.com. For more information
 * on CoolServlets.com, please see <http://www.coolservlets.com>.
 */

package com.Yasna.forum;

import java.util.*;
//JDK1.1// import com.sun.java.util.collections.*;

/**
 * A protection proxy for ForumFactory. It ensures that only authorized users
 * are allowed to access restricted methods.
 */
public class ForumFactoryProxy extends ForumFactory {

    private ForumFactory factory;
    private Authorization authorization;
    private ForumPermissions permissions;

    public ForumFactoryProxy(ForumFactory factory, Authorization authorization,
            ForumPermissions permissions)
    {
        this.factory = factory;
        this.authorization = authorization;
        this.permissions = permissions;
    }

    public Forum createForum(String name, String description,
                             boolean moderated, int forumGroupID)
            throws UnauthorizedException, ForumAlreadyExistsException
    {
        if (permissions.get(ForumPermissions.SYSTEM_ADMIN)) {
            Forum newForum = factory.createForum(name, description,
                                                 moderated, forumGroupID);
            return new ForumProxy(newForum, authorization, permissions);
        }
        else {
            throw new UnauthorizedException();
        }
    }

    public void deleteForum(Forum forum) throws UnauthorizedException {
        if (permissions.get(ForumPermissions.SYSTEM_ADMIN)) {
            factory.deleteForum(forum);
        }
        else {
            throw new UnauthorizedException();
        }
    }

    public void deleteCategory(Category category) throws UnauthorizedException {
        if (permissions.get(ForumPermissions.SYSTEM_ADMIN)) {
            factory.deleteCategory(category);
        }
        else {
            throw new UnauthorizedException();
        }
    }

    public Forum getForum(int forumID) throws ForumNotFoundException,
            UnauthorizedException
    {
        Forum forum = factory.getForum(forumID);
        ForumPermissions forumPermissions = forum.getPermissions(authorization);
        //Create a new permissions object with the combination of the
        //permissions of this object and tempPermissions.
        ForumPermissions newPermissions =
                new ForumPermissions(permissions, forumPermissions);
        //Check and see if the user has READ permissions. If not, throw an
        //an UnauthorizedException.
        if (!(
            newPermissions.get(ForumPermissions.READ) ||
            newPermissions.get(ForumPermissions.FORUM_ADMIN) ||
            newPermissions.get(ForumPermissions.SYSTEM_ADMIN)
            ))
        {
            throw new UnauthorizedException();
        }
        return new ForumProxy(forum, authorization, newPermissions);
    }

    public Forum getForum(String name) throws ForumNotFoundException,
            UnauthorizedException
    {
        Forum forum = factory.getForum(name);
        ForumPermissions forumPermissions = forum.getPermissions(authorization);
        //Create a new permissions object with the combination of the
        //permissions of this object and tempPermissions.
        ForumPermissions newPermissions =
                new ForumPermissions(permissions, forumPermissions);
        //Check and see if the user has READ permissions. If not, throw an
        //an UnauthorizedException.
        if (!(
            newPermissions.get(ForumPermissions.READ) ||
            newPermissions.get(ForumPermissions.FORUM_ADMIN) ||
            newPermissions.get(ForumPermissions.SYSTEM_ADMIN)
            ))
        {
            throw new UnauthorizedException();
        }
        return new ForumProxy(forum, authorization, newPermissions);
    }

    public int getForumCount() {
        return factory.getForumCount();
    }

    public Iterator categories() {
        return new CategoryIteratorProxy(factory.categories(), authorization, permissions);
    }

    public Category getCategory(int categoryID) throws CategoryNotFoundException,
        UnauthorizedException
    {
        Category category = factory.getCategory(categoryID);
        //category permissions can be implemented here
        return new CategoryProxy(category, authorization, permissions);
    }

    public Category getCategory(String name) throws CategoryNotFoundException,
        UnauthorizedException
    {
        Category category = factory.getCategory(name);
        //category permissions can be implemented here
        return new CategoryProxy(category, authorization, permissions);
    }

    public Category createCategory(String name, String description)
            throws UnauthorizedException, CategoryAlreadyExistsException
    {
        if (permissions.get(ForumPermissions.SYSTEM_ADMIN)) {
            Category newCategory = factory.createCategory(name, description);
            return new CategoryProxy(newCategory, authorization, permissions);
        }
        else {
            throw new UnauthorizedException();
        }
    }

    public Iterator forums() {
        return new ForumIteratorProxy(factory.forums(), authorization, permissions);
    }
    public Iterator forumsModeration() {
        return new ForumModeratorIteratorProxy(factory.forums(), authorization, permissions);
    }

    public ProfileManager getProfileManager() {
        ProfileManager profileManager = factory.getProfileManager();
        return new ProfileManagerProxy(profileManager, authorization, permissions);
    }

    public SearchIndexer getSearchIndexer() throws UnauthorizedException {
        if (permissions.get(ForumPermissions.SYSTEM_ADMIN)) {
            return factory.getSearchIndexer();
        }
        else {
            throw new UnauthorizedException();
        }
    }

    public int [] usersWithPermission(int permissionType)
            throws UnauthorizedException
    {
        if (permissions.get(ForumPermissions.SYSTEM_ADMIN)) {
            return factory.usersWithPermission(permissionType);
        }
        else {
            throw new UnauthorizedException();
        }
    }

    public int[] groupsWithPermission(int permissionType)
            throws UnauthorizedException
    {
        if (permissions.get(ForumPermissions.SYSTEM_ADMIN)) {
            return factory.groupsWithPermission(permissionType);
        }
        else {
            throw new UnauthorizedException();
        }
    }

    public ForumPermissions getPermissions(Authorization authorization) {
        return factory.getPermissions(authorization);
    }

    public boolean hasPermission(int type) {
        return permissions.get(type);
    }

    /**
     * Returns the forum factory class that the proxy wraps. In some cases,
     * this is necessary so that an outside class can get at methods that only
     * a particular forum factory sublclass contains. Because this is
     * potentially a dangerours operation, access to the underlying class is
     * restricted to those with SYSTEM_ADMIN permissions.
     *
     * @throws UnauthorizedException if does not have ADMIN permissions.
     */
    public ForumFactory getUnderlyingForumFactory()
            throws UnauthorizedException
    {
        if (permissions.get(ForumPermissions.SYSTEM_ADMIN)) {
            return factory;
        }
        else {
            throw new UnauthorizedException();
        }
    }
}
