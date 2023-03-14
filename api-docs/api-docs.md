# Spring API Documentation

###### (Auto-generated)

---

## Global Error Format

```json
ErrorFormat {
	detail: string ,
	internalCode: number ,
	title: string 
}
```
---

## Users

- ### GET /api/users/{id: string}

#### Response Body

```json
User {
	createdAt: Date ,
	id: string ,
	name: string 
}
```
#### Success Responses

`200` OK

#### Error Responses

`404` NOT_FOUND

`422` UNPROCESSABLE_ENTITY

- ### DELETE /api/users/{id: string}

#### Response Body

```json
void
```
#### Success Responses

`204` NO_CONTENT

#### Error Responses

`422` UNPROCESSABLE_ENTITY

`404` NOT_FOUND

- ### POST /api/users/

#### Request Body

```json
CreateUserModel {
	name: string 
}
```
#### Response Body

```json
User {
	createdAt: Date ,
	id: string ,
	name: string 
}
```
#### Success Responses

`201` CREATED

#### Error Responses

`404` NOT_FOUND

`422` UNPROCESSABLE_ENTITY



---

## Posts

- ### POST /api/posts/

#### Request Body

```json
CreatePostModel {
	authorId: string ,
	content?: string | null,
	title: string 
}
```
#### Response Body

```json
Post {
	author: User ,
	content?: string | null,
	createdAt: Date ,
	id: string ,
	title: string 
}
```
#### Success Responses

`201` CREATED

#### Error Responses

`422` UNPROCESSABLE_ENTITY

`404` NOT_FOUND

- ### GET /api/posts/{id: string}

#### Response Body

```json
Post {
	author: User ,
	content?: string | null,
	createdAt: Date ,
	id: string ,
	title: string 
}
```
#### Success Responses

`200` OK



