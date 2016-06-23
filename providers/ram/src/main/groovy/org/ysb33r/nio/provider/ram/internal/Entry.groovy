/*
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2016
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
package org.ysb33r.nio.provider.ram.internal

import groovy.transform.CompileStatic

/**
 * @author Schalk W. Cronjé
 */
@CompileStatic
interface Entry {
    /** Is this entry a file?
     *
     * @return {@code true} if the entry is a file.
     */
    boolean isFile()

    /** Is this entry a directory?
     *
     * @return {@code true} if the entry is a directory.
     */
    boolean isDirectory()

    /** Size of the entry.
     *
     * @return Returns sizes. In case of a directory it is allwoed to be zero.
     */
    long size()
}