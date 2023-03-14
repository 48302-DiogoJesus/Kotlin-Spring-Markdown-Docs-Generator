```json
User {
	createdAt: Date ,
	id: string ,
	name: string 
}
```

```json
ErrorFormat {
	detail: string ,
	internalCode: number ,
	title: string 
}
```

```json
CreatePostModel {
	authorId: string ,
	content?: string | null,
	title: string 
}
```

```json
Post {
	author: User ,
	content?: string | null,
	createdAt: Date ,
	id: string ,
	title: string 
}
```

```json
CreateUserModel {
	name: string 
}
```