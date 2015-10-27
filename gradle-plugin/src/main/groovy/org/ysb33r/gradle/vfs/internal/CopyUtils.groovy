/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2015
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * ============================================================================
 */


package org.ysb33r.gradle.vfs.internal

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.commons.vfs2.FileObject
import org.gradle.api.logging.Logger
import org.ysb33r.gradle.vfs.VfsCopySpec
import org.ysb33r.gradle.vfs.VfsURI
import org.ysb33r.gradle.vfs.VfsURICollection
import org.ysb33r.groovy.dsl.vfs.VFS
import org.ysb33r.groovy.dsl.vfs.impl.Util

/** Utilities for VFS copy operations.
 *
 * @author Schalk W. Cronjé
 * @since 1.0
 */
@CompileStatic
class CopyUtils {

    @CompileDynamic
    static void copyChildren( VFS vfs, VfsURI src, VfsURI destRoot ) {
        vfs.cp src.praxis, src.uri, destRoot.uri
//        vfs.call {
//            ls src.uri, { fo ->
//                ++count
//                cp src.praxis, fo, destRoot.uri
//            }
//        }
    }

    /** Copies a set of sources to a destination, logging success along the way
     *
     * @param logger Logger to report progress
     * @param vfs Virtual file system to use
     * @param sources Source URIs
     * @param destRoot Destination destRoot URI. If it does not exit it will be created prior to starting the copy
     * @since 1.0
     */
    static void copy( Logger logger, VFS vfs, Set<VfsURI> sources, VfsURI destRoot ) {

        if(!vfs.exists(destRoot.uri)) {
            vfs.mkdir destRoot.uri, intermediates:true
        }

        sources.each { VfsURI src ->
            if( vfs.fsCanListFolderContent(src.uri) && vfs.isFolder(src.uri) ) {
                copyChildren( vfs, src, destRoot )
                logger.info "Copied ${friendlyURI(vfs,src)} -> ${friendlyURI(vfs,destRoot)}"
            } else {
                vfs.cp src.praxis, src.uri, destRoot.uri
                logger.info "Copied ${friendlyURI(vfs,src)} -> ${friendlyURI(vfs,destRoot)}"
            }
        }
    }

    /** On a filesystem/scheme that can list folder contents it performs a deep (recursive) copy of all sources in a
     * spec including all children specS.  In order to keep the behaviour of normal {@code CopySpec}, the base folder
     * of the source  will be dropped from the destination path.
     * If the contents cannot be listed, the source spec is treated as file instead resulting in any
     * include/exclude filters being ignored.
     *
     * @param logger Logger to report progress
     * @param vfs Virtual file system to use
     * @param rootSpec Root copy specification
     * @param destRoot Destination destRoot URI. If it does not exit it will be created prior to starting the copy
     * @since 1.0
     */
    static void recursiveCopy( Logger logger, VFS vfs, VfsCopySpec rootSpec, VfsURI destRoot) {
        VfsURI dest = destRoot.resolve()
        CopyUtils.copy(logger,vfs,rootSpec.uriCollection.uris,dest)

        rootSpec.children().each {
            VfsCopySpec child = it as VfsCopySpec
            VfsURI childDest = ResolvedURI.create([:],vfs,Util.addRelativePath(dest.uri as FileObject,child.relativePath))
            recursiveCopy(logger,vfs,child,childDest)
        }
    }

    /** Returns a set of all of the full resolved destination URIs in a copy specification
     *
     * @param vfs Virtual file system to use
     * @param rootSpec Root copy specification
     * @param root Destination root URI. If it does not exit it will be created prior to starting the copy
     * @return Destination collection. Never null
     */
    static VfsURICollection recursiveDestinationList( VFS vfs,VfsCopySpec rootSpec, VfsURI root) {
        VfsURICollection ret = new DefaultVfsURICollection()

        VfsURI dest = root.resolve()
        ret.add(dest)
        rootSpec.children().each {
            VfsCopySpec child = it as VfsCopySpec
            VfsURI childDest = ResolvedURI.create([:], vfs, Util.addRelativePath(dest.uri as FileObject, child.relativePath))
            ret.add(childDest)
            VfsURICollection childCollection = recursiveDestinationList(vfs,child,childDest)
            if(!ret.empty) {
                ret = ret + childCollection
            }
        }
        ret
    }

    @CompileDynamic
    private static String friendlyURI(vfs,VfsURI uri) {
        vfs.friendlyURI( uri.uri )
    }

}
