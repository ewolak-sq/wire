/*
 * Copyright 2021 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.wire.reflector

import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.squareup.wire.schema.Location
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.SchemaLoader
import grpc.reflection.v1alpha.ServerReflectionRequest
import grpc.reflection.v1alpha.ServerReflectionResponse
import okio.ByteString.Companion.decodeBase64
import okio.FileSystem
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

internal class GrpcurlProto3InteropTest {
  @Test
  fun `list services`() {
    val schema = loadSchema()
    val respBase64 = "EgM6ASoyRQoqCihncnBjLnJlZmxlY3Rpb24udjFhbHBoYS5TZXJ2ZXJSZWZsZWN0aW9uChcKFXJvdXRlZ3VpZGUuUm91dGVHdWlkZQ=="
    val expectedResponse = ServerReflectionResponse.ADAPTER.decode(respBase64.decodeBase64()!!)
    assertThat(
      SchemaReflector(schema).process(
        ServerReflectionRequest(
          list_services = "*"
        )
      )
    ).isEqualTo(expectedResponse)
  }
  @Test
  fun `file_containing_symbol`() {
    val schema = loadSchema()
    val respBase64 = "EhciFXJvdXRlZ3VpZGUuUm91dGVHdWlkZSLnBgrkBgoXcmd1aWRlL3JvdXRlZ3VpZGUucHJvdG8SCnJvdXRlZ3VpZGUiQQoFUG9pbnQSGgoIbGF0aXR1ZGUYASABKAVSCGxhdGl0dWRlEhwKCWxvbmdpdHVkZRgCIAEoBVIJbG9uZ2l0dWRlIlEKCVJlY3RhbmdsZRIhCgJsbxgBIAEoCzIRLnJvdXRlZ3VpZGUuUG9pbnRSAmxvEiEKAmhpGAIgASgLMhEucm91dGVndWlkZS5Qb2ludFICaGkiTAoHRmVhdHVyZRISCgRuYW1lGAEgASgJUgRuYW1lEi0KCGxvY2F0aW9uGAIgASgLMhEucm91dGVndWlkZS5Qb2ludFIIbG9jYXRpb24iVAoJUm91dGVOb3RlEi0KCGxvY2F0aW9uGAEgASgLMhEucm91dGVndWlkZS5Qb2ludFIIbG9jYXRpb24SGAoHbWVzc2FnZRgCIAEoCVIHbWVzc2FnZSKTAQoMUm91dGVTdW1tYXJ5Eh8KC3BvaW50X2NvdW50GAEgASgFUgpwb2ludENvdW50EiMKDWZlYXR1cmVfY291bnQYAiABKAVSDGZlYXR1cmVDb3VudBIaCghkaXN0YW5jZRgDIAEoBVIIZGlzdGFuY2USIQoMZWxhcHNlZF90aW1lGAQgASgFUgtlbGFwc2VkVGltZTK6AgoKUm91dGVHdWlkZRI0CgpHZXRGZWF0dXJlEhEucm91dGVndWlkZS5Qb2ludBoTLnJvdXRlZ3VpZGUuRmVhdHVyZRI7ChFHZXREZWZhdWx0RmVhdHVyZRIRLnJvdXRlZ3VpZGUuUG9pbnQaEy5yb3V0ZWd1aWRlLkZlYXR1cmUSPAoMTGlzdEZlYXR1cmVzEhUucm91dGVndWlkZS5SZWN0YW5nbGUaEy5yb3V0ZWd1aWRlLkZlYXR1cmUwARI8CgtSZWNvcmRSb3V0ZRIRLnJvdXRlZ3VpZGUuUG9pbnQaGC5yb3V0ZWd1aWRlLlJvdXRlU3VtbWFyeSgBEj0KCVJvdXRlQ2hhdBIVLnJvdXRlZ3VpZGUuUm91dGVOb3RlGhUucm91dGVndWlkZS5Sb3V0ZU5vdGUoATABQihaJmdpdGh1Yi5jb20vanVsaWFvZ3Jpcy9ndXBweS9wa2cvcmd1aWRlYgZwcm90bzM="
    val expectedResponse = ServerReflectionResponse.ADAPTER.decode(respBase64.decodeBase64()!!)
    val response = SchemaReflector(schema).process(
      ServerReflectionRequest(
        file_containing_symbol = "routeguide.RouteGuide"
      )
    )
    assertThat(response.fileDescriptors).isEqualTo(expectedResponse.fileDescriptors)
  }

  private val ServerReflectionResponse.fileDescriptors
    get() = file_descriptor_response!!.file_descriptor_proto.map {
      FileDescriptorProto.parseFrom(
        it.toByteArray()
      )
    }

  private fun loadSchema(): Schema {
    return SchemaLoader(FileSystem.SYSTEM)
      .apply {
        initRoots(
          sourcePath = listOf(
            Location.get("src/jvmMain/proto"),
            Location.get("src/jvmTest/proto", "rguide/routeguide.proto"),
          ),
          protoPath = listOf()
        )
      }
      .loadSchema()
  }
}
